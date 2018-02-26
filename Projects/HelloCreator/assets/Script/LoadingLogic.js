// Learn cc.Class:
//  - [Chinese] http://www.cocos.com/docs/creator/scripting/class.html
//  - [English] http://www.cocos2d-x.org/docs/editors_and_tools/creator-chapters/scripting/class/index.html
// Learn Attribute:
//  - [Chinese] http://www.cocos.com/docs/creator/scripting/reference/attributes.html
//  - [English] http://www.cocos2d-x.org/docs/editors_and_tools/creator-chapters/scripting/reference/attributes/index.html
// Learn life-cycle callbacks:
//  - [Chinese] http://www.cocos.com/docs/creator/scripting/life-cycle-callbacks.html
//  - [English] http://www.cocos2d-x.org/docs/editors_and_tools/creator-chapters/scripting/life-cycle-callbacks/index.html

cc.Class({
    extends: cc.Component,

    properties: {
        tipLabel: cc.Label,
        _stateStr:'',
    },

    // LIFE-CYCLE CALLBACKS:

    // use this for initialization
    onLoad: function () {
        if (!cc.sys.isNative && cc.sys.isMobile) {
            var cvs = this.node.getComponent(cc.Canvas);
            cvs.fitHeight = true;
            cvs.fitWidth = true;
        }
        this.initMgr();
        //this.tipLabel.string = this._stateStr;
    },

    onDestroy: function () {

    },

    initMgr: function() {
        cc.vv = {};
        var UserMgr = require("UserMgr");
        cc.vv.userMgr = new UserMgr();
    },

    onLoadComplete: function() {
        cc.director.loadScene("Login");
    },

    update: function (dt) {
        if(this._stateStr.length == 0) {
            return;
        }
        this.tipLabel.string = this._stateStr + ' ';
        if (this._isLoading) {
            this.tipLabel.string += Math.floor(this._progress * 100) + "%";
        } else {
            var t = Math.floor(Date.now() / 1000) % 4;
            for (var i = 0; i < t; i++) {
                this.tipLabel.string += '.';
            }
        }
    },


});
