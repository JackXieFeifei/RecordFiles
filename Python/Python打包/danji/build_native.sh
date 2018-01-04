#!/bin/bash
APPNAME="LandlordDJ"

# options
buildexternalsfromsource=

usage(){
cat << EOF
usage: $0 [options]

Build C/C++ code for $APPNAME using Android NDK

OPTIONS:
-s	Build externals from source
-h	this help
EOF
}

while getopts "sh" OPTION; do
case "$OPTION" in
s)
buildexternalsfromsource=1
;;
h)
usage
exit 0
;;
esac
done

# paths

if [ -z "${NDK_ROOT+aaa}" ];then
echo "please define NDK_ROOT"
exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


# ... use paths relative to current directory

COCOS2DX_ROOT="$DIR/../../Libraries"
APP_ROOT="$DIR/../.."
APP_ANDROID_ROOT="$DIR"

echo "NDK_ROOT = $NDK_ROOT"
echo "COCOS2DX_ROOT = $COCOS2DX_ROOT"
echo "APP_ROOT = $APP_ROOT"
echo "APP_ANDROID_ROOT = $APP_ANDROID_ROOT"

# make sure assets is exist
if [ -d "$APP_ANDROID_ROOT"/assets ]; then
    rm -rf "$APP_ANDROID_ROOT"/assets
fi

mkdir "$APP_ANDROID_ROOT"/assets

# copy common resources
for file in "$APP_ROOT"/Resources/iPad/*
do
if [ -d "$file" ]; then
    cp -rf "$file" "$APP_ANDROID_ROOT"/assets
fi

if [ -f "$file" ]; then
    cp "$file" "$APP_ANDROID_ROOT"/assets
fi
done

# copy android resources
for file in "$APP_ANDROID_ROOT"/resource/*
do
	if [ -d "$file" ]; then
		cp -rf "$file" "$APP_ANDROID_ROOT"/assets
	fi
	
	if [ -f "$file" ]; then
  	cp "$file" "$APP_ANDROID_ROOT"/assets
  fi
done

chmod -R 776 "$APP_ANDROID_ROOT"/assets

#delete unused resources
rm -rf "$APP_ANDROID_ROOT"/assets/CCBProj
rm -rf "$APP_ANDROID_ROOT"/assets/Icons
rm -rf "$APP_ANDROID_ROOT"/assets/Fonts

rm "$APP_ANDROID_ROOT"/assets/Info.plist

find "$APP_ANDROID_ROOT"/assets -name '.svn' | xargs rm -rf

chmod -R 776 "$APP_ANDROID_ROOT"/assets

if [[ "$buildexternalsfromsource" ]]; then
    echo "Building external dependencies from source"
    "$NDK_ROOT"/ndk-build NDK_DEBUG=0 -C "$APP_ANDROID_ROOT" $* \
        "NDK_MODULE_PATH=${COCOS2DX_ROOT}:${COCOS2DX_ROOT}/cocos2dx/platform/third_party/android/source"
else
    echo "Using prebuilt externals"
    "$NDK_ROOT"/ndk-build NDK_DEBUG=0 -C "$APP_ANDROID_ROOT" $* \
        "NDK_MODULE_PATH=${COCOS2DX_ROOT}:${COCOS2DX_ROOT}/cocos2dx/platform/third_party/android/prebuilt"
fi

# copy android *.so libraries
for file in "$APP_ANDROID_ROOT"/so/*
do
	if [ -d "$file" ]; then
		cp -rf "$file" "$APP_ANDROID_ROOT"/libs/armeabi
	fi
	
	if [ -f "$file" ]; then
  	cp "$file" "$APP_ANDROID_ROOT"/libs/armeabi
  fi
done
