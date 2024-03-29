Ext.ns('App.View');

App.View.Viewport = Ext.extend(Ext.Panel, {
	id        : 'viewport',
	layout    : 'card',
	fullscreen: true,

	initComponent: function() {
		var config = {};
		Ext.apply(this, config);
		App.View.Viewport.superclass.initComponent.call(this);
	}

});

Ext.reg('App.View.Viewport', App.View.Viewport);