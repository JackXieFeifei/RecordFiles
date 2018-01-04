#!/usr/bin/python  
#-*-coding:utf-8-*-

#version: 0.5.6

import sys
import os
import shutil
import stat
import json
import re
import copy
import codecs
import argparse
from xml.dom import minidom


PEIZHI_STR = 'build_gradle_config.json'
''' 测试用打包配置： build_config.json     渠道包打包配置： build_config_qudao.json     公版包打包配置： build_config_gongban.json
'''

class AndroidBuilder:
    def __init__(self, app_root='.'):
        self.load_environment()
        self.load_config(PEIZHI_STR)
        self.init_backup()
        
        self.app_root = app_root
        self.channel = ''
        self.channel_name = ''
        self.version = ''
        self.package = ''
        self.environment = 'release'
    
    def load_environment(self):
        ''' Load environment of Android SDK, NDK and GRADLE from 'local.properties'
        '''
        self.env = {}
        fp = open('local.properties', 'r')
        for line in fp.readlines():
            line = line.strip()
            if len(line) > 0 and not line.startswith('#'):
                (key, value) = tuple(line.split('='))
                self.env[key] = value
        fp.close()
        print 'sdk.dir = ' + self.env['sdk.dir']
        print 'ndk.dir = ' + self.env['ndk.dir']
        print 'gradle.dir = ' + self.env['gradle.dir']
    
    def load_config(self, path):
        ''' Load configuration for all channels
        '''
        fp = open(path, 'r')
        json_object = json.load(fp, 'utf-8')
        self.base_actions = json_object['actions']
        self.channels = json_object['channels']
        self.prefabs = {}
        if 'prefabs' in json_object:
            self.prefabs = json_object['prefabs']
        fp.close()
    
    def init_backup(self):
        ''' Initialize backup files.
            Only 'res', 'libs', 'src' and 'AndroidManifest.xml' need to be backed up
        '''
        self.changes = []
        self.clean_backup()
        os.mkdir('_backup')
        shutil.copytree('res', '_backup/res')
        shutil.copytree('libs', '_backup/libs')
        shutil.copytree('src', '_backup/src')
        shutil.copy('AndroidManifest.xml', '_backup')
    
    def clean_backup(self):
        if os.path.exists('_backup'):
            shutil.rmtree('_backup')
    
    def restore_backup(self):
        for path in self.changes:
            back_path = os.path.join('_backup', path)
            if os.path.isfile(back_path):
                shutil.copyfile(back_path, path)
            elif os.path.isdir(back_path):
                shutil.rmtree(path)
                shutil.copytree(back_path, path)
            elif os.path.isfile(path):
                os.remove(path)
            elif os.path.isdir(path):
                shutil.rmtree(path)
        self.changes = []
    
    def backup(self, path):
        relpath = os.path.relpath(path)
        if not relpath in self.changes and self.need_backup(relpath):
            self.changes.append(relpath)
    
    def need_backup(self, path):
        ''' Check if a file need to be backed up.
        '''
        return self._is_in_dir(path, 'res') or self._is_in_dir(path, 'libs') or \
        self._is_in_dir(path, 'src') or path.lower() == 'androidmanifest.xml'
    
    def ndk_build(self, build_param, module_path):
        ''' Use 'ndk-build' to build dynamic or static C/C++ libraries
        '''
        ndk_path = os.path.join(self.env['ndk.dir'], "ndk-build")
        ndk_path = os.path.realpath(ndk_path)
        build_param = build_param and ''
        
        command = '%s -j%d -C %s %s %s' % \
        (ndk_path, self._get_num_of_cpu(), self.app_root, build_param, module_path)
            
        if os.system(command) != 0:
            raise Exception('NDK build failed!')

    def gradle_build(self, build_param):
        ''' Use android tools to build and generate an android APK
        '''
        gradle_path = os.path.join(self.env['gradle.dir'], 'gradle')
        command = '%s assembleRelease' % (gradle_path)
        
        if os.system(command) != 0:
            raise Exception('gradle build failed!')
    
    def copy(self, src, dst):
        ''' Copy a file or a whole directory to destination folder
        '''
        if os.path.isdir(src):
            for item in os.listdir(src):
                src_path = os.path.join(src, item)
                dst_path = os.path.join(dst, item)
                if os.path.isfile(src_path) and not item.startswith('.'):
                    self.copy(src_path, dst_path)
                elif os.path.isdir(src_path) and item != '.svn':
                    if not os.path.isdir(dst_path):
                        os.makedirs(dst_path)
                    self.copy(src_path, dst_path)
        elif os.path.isfile(src):
            if os.path.isdir(dst):
                dst = os.path.join(dst, os.path.basename(src))
            elif not os.path.isdir(os.path.dirname(dst)):
                os.makedirs(os.path.dirname(dst))
            self.backup(dst)
            shutil.copy(src, dst)
        else:
            print 'Warning: Try to copy a non-existent file: ' + src
                
    def remove(self, path):
        ''' Remove a file or a whole directory
        '''
        self.backup(path)
            
        if os.path.isdir(path):
            for root, dirs, files in os.walk(path, topdown=False):
                for name in files:
                    file_name = os.path.join(root, name)
                    os.chmod(file_name, stat.S_IWUSR)
                    os.remove(file_name)
                for name in dirs:
                    os.rmdir(os.path.join(root, name))
            os.rmdir(path);
        elif os.path.isfile(path):
            os.remove(path)
    
    def rename(self, src, dst):
        ''' Rename a file to a new name
        '''
        if os.path.exists(dst):
            self.remove(dst)
        self.backup(src)
        self.backup(dst)
        os.renames(src, dst)
        
    def replace(self, path, old, new):
        ''' Find and replace strings in a file or directory
        '''
        self.backup(path)
        
        old = self._realpath(old)
        new = self._realpath(new)
        if os.path.isdir(path):
            for root, dirs, files in os.walk(path, topdown=False):
                for file in files:
                    file_path = os.path.join(root, file)
                    if file_path.find('.svn') == -1 and file_path.find('.DS_Store') == -1:
                        self._replace_file(file_path, old, new)
        elif os.path.isfile(path) and path.find('.DS_Store') == -1:
            self._replace_file(path, old, new)
        else:
            print 'Warning: Nothing to be replaced, can not find %s!' % path
			
    def insert(self, path_src, path_dest, pos):
        ''' Insert the contents form a file to another file			
		    @path_src   the path of source file
            @path_dest  the path of destination file			
			@pos        the position of insert		
		'''
        file = open(path_dest,"r")
        fileadd = open(path_src,"r")
        content = file.read()
        contentadd = fileadd.read()
        file.close()
        fileadd.close()
        pos = content.find(pos)
        if pos != -1:
            content = content[:pos] + contentadd + content[pos:]
            file = open(path_dest,"w")
            file.write(content)
            file.close
            
    def add_permission(self, permission):
        ''' Add a new 'uses-permission' node into AndroidManifest.xml
        '''
        self.delete_permission(permission)
        dom = minidom.parse('AndroidManifest.xml')
        manifest = dom.documentElement
        permissions = manifest.getElementsByTagName('uses-permission')
        node = dom.createElement('uses-permission')
        node.setAttribute('android:name', permission)
        if len(permissions) > 0:
            manifest.insertBefore(node, permissions[0])
        else:
            manifest.appendChild(node)
        fp = codecs.open('AndroidManifest.xml', 'w', 'utf-8')
        dom.writexml(fp, newl='', encoding='utf-8')
        fp.close()
    
    def delete_permission(self, permission):
        ''' Delete a permission from AndroidManifest.xml
        '''
        dom = minidom.parse('AndroidManifest.xml')
        manifest = dom.documentElement
        permissions = manifest.getElementsByTagName('uses-permission')
        for item in permissions:
            if item.getAttribute('android:name') == permission:
                manifest.removeChild(item)
        fp = codecs.open('AndroidManifest.xml', 'w', 'utf-8')
        dom.writexml(fp, newl='', encoding='utf-8')
        fp.close()
        
    def copy_xml_node(self, src, src_route, dst, dst_route):
        ''' Copy a node from a xml file to another, nodes are identified by routes
        '''
        src_element = self._get_xml_node(minidom.parse(src), src_route)
        dst_dom = minidom.parse(dst)
        dst_element = self._get_xml_node(dst_dom, dst_route)
        if src_element and dst_element:
            dst_element.appendChild(src_element)
        fp = codecs.open(dst, 'w', 'utf-8')
        dst_dom.writexml(fp, newl='', encoding='utf-8')
        fp.close()
        
    def copy_xml_child_nodes(self, src, src_route, dst, dst_route):
        ''' Copy all children of a node from a xml file to another
        '''
        src_element = self._get_xml_node(minidom.parse(src), src_route)
        dst_dom = minidom.parse(dst)
        dst_element = self._get_xml_node(dst_dom, dst_route)
        if src_element and dst_element:
            for child in src_element.childNodes:
                dst_element.appendChild(copy.deepcopy(child))
        fp = codecs.open(dst, 'w', 'utf-8')
        dst_dom.writexml(fp, newl='', encoding='utf-8')
        fp.close()
        
    def delete_xml_node(self, path, route):
        ''' Delete a node from a xml file
        '''
        dom = minidom.parse(path)
        element = self._get_xml_node(dom, route)
        if element and element.parentNode is not None:
            element.parentNode.removeChild(element)
        fp = codecs.open(path, 'w', 'utf-8')
        dom.writexml(fp, newl='', encoding='utf-8')
        fp.close()

    def set_meta_data(self, name, value):
        dom = minidom.parse('AndroidManifest.xml')
        manifest = dom.documentElement
        meta_data_list = manifest.getElementsByTagName('meta-data')
        for meta_data in meta_data_list:
            _name = meta_data.getAttribute('android:name')
            if _name == name:
                meta_data.setAttribute('android:value', value)
                fp = codecs.open('AndroidManifest.xml', 'w', 'utf-8')
                dom.writexml(fp, newl='', encoding='utf-8')
                fp.close()
                return None
        meta_data = dom.createElement('meta-data')
        meta_data.setAttribute('android:name', name)
        meta_data.setAttribute('android:value', value)
        application = manifest.getElementsByTagName('application')
        application[0].appendChild(meta_data)
        fp = codecs.open('AndroidManifest.xml', 'w', 'utf-8')
        dom.writexml(fp, newl='', encoding='utf-8')
        fp.close()
        
    def _get_xml_node(self, dom, route):
        element = dom.documentElement
        nodes = re.findall(r'\<([\w\s\-\=\.\:]+)\>', route)
        for name in nodes:
            pos = name.find(' ')
            if pos >= 0:
                tag = name[0:pos]
                (key, value) = tuple(name[pos+1:].split('='))
                elements = element.getElementsByTagName(tag)
                for item in elements:
                    if item.getAttribute(key) == value:
                        element = item
                        break
            else:
                element = element.getElementsByTagName(name)[0]
        return element
    
    def _replace_file(self, path, old, new):
        print path, old, new
        fp = codecs.open(path, 'r', 'utf-8')
        data = fp.read()
        data = data.replace(old, new)
        fp.close()
        fp = codecs.open(path, 'w', 'utf-8')
        fp.write(data)
        fp.close()
    
    def _is_in_dir(self, file, dir):
        ''' Check if a file is in a directory
        '''
        file_path = os.path.abspath(file)
        dir_path = os.path.abspath(dir)
        return file_path.find(dir_path) != -1
    
    def _realpath(self, path):
        ''' Get real path from the path with variables
        '''
        path = path.replace('$channelname', self.channel_name)
        path = path.replace('$channel', self.channel)
        path = path.replace('$environment', self.environment)
        path = path.replace('$version', self.version)
        path = path.replace('$package', self.package)
        return path
    
    def do_action(self, action):
        if 'copy' in action:
            src = self._realpath(action['copy']['from'])
            dst = self._realpath(action['copy']['to'])
            print 'Copying files from "%s" to "%s"...' % (src, dst)
            self.copy(src, dst)
        elif 'remove' in action:
            path = self._realpath(action['remove'])
            print 'Removing "%s"...' % path
            self.remove(path)
        elif 'rename' in action:
            src = self._realpath(action['rename']['from'])
            dst = self._realpath(action['rename']['to'])
            print 'Rename "%s" to "%s"' % (src, dst)
            self.rename(src, dst)
        elif 'replace' in action:
            action = action['replace']
            path = self._realpath(action['path'])
            print 'Replace "%s" to "%s" in %s' % (action['old'], action['new'], path)
            self.replace(path, action['old'], action['new'])
        elif 'insert' in action:
            action = action['insert']
            path_src = self._realpath(action['from'])
            path_dest = self._realpath(action['to'])
            pos = self._realpath(action['position'])
            print 'Insert "%s" to "%s" at "%s"' % (path_src,path_dest,pos)
            self.insert(path_src,path_dest,pos)
        elif 'ndk-build' in action:
            parameter = action['ndk-build']['parameter']
            module_path = 'NDK_MODULE_PATH=' + action['ndk-build']['module-path']
            # windows should use ";" to separate module paths
            if sys.platform == 'win32':
                module_path = module_path.replace(':', ';')
            self.ndk_build(parameter, module_path)
        elif 'gradle-build' in action:
            self.gradle_build(action['gradle-build'])
        elif 'add-permission' in action:
            print 'Add permission %s to AndroidManifest.xml' % action['add-permission']
            self.add_permission(action['add-permission'])
        elif 'delete-permission' in action:
            print 'Delete permission %s from AndroidManifest.xml' % action['delete-permission']
            self.delete_permission(action['delete-permission'])
        elif 'command' in action:
            for cmd in action['command']:
                print 'call command:', cmd
                os.system(cmd)
        elif 'copy-xml-node' in action:
            act = action['copy-xml-node']
            self.copy_xml_node(act['src'], act['src-route'], act['dst'], act['dst-route'])
        elif 'copy-xml-child-nodes' in action:
            act = action['copy-xml-child-nodes']
            self.copy_xml_child_nodes(act['src'], act['src-route'], act['dst'], act['dst-route'])
        elif 'delete-xml-node' in action:
            act = action['delete-xml-node']
            self.delete_xml_node(act['path'], act['route'])
        elif 'set-meta-data' in action:
            act = action['set-meta-data']
            self.set_meta_data(act['name'], act['value'])
        elif isinstance(action, dict):
            for name in action:
                self.do_action(action[name])
                break
        elif isinstance(action, list):
            for sub_action in action:
                self.do_action(sub_action)
        elif isinstance(action, basestring):
            if action in self.prefabs:
                self.do_action(self.prefabs[action])
                
    def config(self, channel_config):
        self.backup('AndroidManifest.xml')
        
        if isinstance(channel_config, basestring):
            config = {'channel': str(channel_config)}
        elif 'channel' in channel_config:
            config = channel_config
        channel = config['channel']
        
        # Configure package name in 'AndroidManifest.xml'
        dom = minidom.parse('AndroidManifest.xml')
        manifest = dom.documentElement
        old_package = manifest.getAttribute('package')
        if 'package' in config and old_package != config['package']:
            self.replace('AndroidManifest.xml', old_package, config['package'])
        
        dom = minidom.parse('AndroidManifest.xml')
        manifest = dom.documentElement
        
        # Configure channel
        meta_data_list = manifest.getElementsByTagName('meta-data')
        for meta_data in meta_data_list:
            name = meta_data.getAttribute('android:name')
            if name == 'UMENG_CHANNEL' or name == 'OG_APPCHANNEL' or name == 'BUGLY_APP_CHANNEL' or name == 'ClientChId':
                meta_data.setAttribute('android:value', channel)
            if 'egamechannel' in channel_config and name == 'EGAME_CHANNEL':
                meta_data.setAttribute('android:value', channel_config['egamechannel'])
        
        # Configure package name in 'src'
        if 'package' in channel_config:
            if old_package != channel_config['package']:
                self.replace('src', old_package, channel_config['package'])
            manifest.setAttribute('package', channel_config['package'])
        self.package = manifest.getAttribute('package')
            
        # Configure version
        if 'version' in channel_config:
            manifest.setAttribute('android:versionName', channel_config['version'])
        self.version = manifest.getAttribute('android:versionName')

        # Configure version code
        if 'versioncode' in channel_config:
            manifest.setAttribute('android:versionCode', channel_config['versioncode'])
        
        fp = codecs.open('AndroidManifest.xml', 'w', 'utf-8')
        dom.writexml(fp, newl='', encoding='utf-8')
        fp.close()
        
        #Configure application name
        if 'name' in channel_config:
            path = 'res/values-zh/strings.xml'
            if os.path.exists(path):
                self.backup(path)
                dom = minidom.parse(path)
                strings = dom.documentElement.getElementsByTagName('string')
                for s in strings:
                    name = s.getAttribute('name')
                    if name == 'app_name':
                        s.firstChild.nodeValue = channel_config['name']
                fp = codecs.open(path, 'w', 'utf-8')
                dom.writexml(fp, newl='', encoding='utf-8')
                fp.close()
                
            path = 'res/values/strings.xml'
            if os.path.exists(path):
                self.backup(path)
                dom = minidom.parse(path)
                strings = dom.documentElement.getElementsByTagName('string')
                for s in strings:
                    name = s.getAttribute('name')
                    if name == 'app_name':
                        s.firstChild.nodeValue = channel_config['name']
                fp = codecs.open(path, 'w', 'utf-8')
                dom.writexml(fp, newl='', encoding='utf-8')
                fp.close()
        
        self.actions = copy.deepcopy(self.base_actions)
        
        #Configure custom actions
        if isinstance(channel_config, dict):
            for name in channel_config:
                config = channel_config[name]
                if isinstance(config, list) and len(config) > 0:
                    for action in self.actions:
                        if name in action:
                            action[name] = config
                            break
    
    def build(self, channel_config_list):
        ''' Build one package with specified channel configuration
        '''
        for channel_config in channel_config_list:
            self.channel = self.get_channel(channel_config)
            self.channel_name = self.get_channel_name(channel_config)
            self.config(channel_config)
            for action in self.actions:
                self.do_action(action)
            self.restore_backup()

    def build_channel(self, channel_name_list):
        config_list = []
        for channel_name in channel_name_list:
            config_list.extend(self.get_channel_config(channel_name))
        #Remove duplicate config(dict) from config_list(list)
        #config_list = list(set(config_list)) #This doesn't work
        config_list = reduce(lambda x, y: x if y in x else x + [y], [[], ] + config_list)
        self.build(config_list)
        self.clean_backup()
        
    def build_all(self):
        ''' Build all packages
        '''
        self.build(self.channels)
        self.clean_backup()
            
    def get_channel(self, channel_config):
        if isinstance(channel_config, basestring):
            return channel_config
        elif 'channel' in channel_config:
            return channel_config['channel']
    
    def get_channel_name(self, channel_config):
        if isinstance(channel_config, dict) and 'channelname' in channel_config:
            return channel_config['channelname']
        return self.get_channel(channel_config)
        
    def get_channel_config(self, channel_name):
        config_list = [config for config in self.channels\
                       if 'channelname' in config and config['channelname'] == channel_name]
        if not config_list:
            config_list = [config for config in self.channels\
                           if 'channel' in config and config['channel'] == channel_name]
        return config_list
    
    @staticmethod
    def _get_num_of_cpu():
        try:
            if sys.platform == 'win32':
                if 'NUMBER_OF_PROCESSORS' in os.environ:
                    return int(os.environ['NUMBER_OF_PROCESSORS'])
            else:
                from numpy.distutils import cpuinfo
                return cpuinfo.cpu._getNCPUs()
        except Exception:
            print "Don't know CPU info, use default 1 CPU"
        return 1

# -------------- main --------------
if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('-c', '--channelnamelist', help='Build one or more channels', nargs='+')
    args = parser.parse_args()
    
    current_dir = os.path.dirname(os.path.realpath(__file__))
    builder = AndroidBuilder(current_dir)
    if args.channelnamelist:
        builder.build_channel(args.channelnamelist)
    else:
        builder.build_all()
    
