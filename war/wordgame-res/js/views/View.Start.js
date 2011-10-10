Ext.ns('App.View');

App.View.Start = Ext.extend(Ext.Panel, {
	
	initComponent: function() {
		var config = {
			layout: 'vbox',
			items: [
			        { xtype: 'spacer' },
			        { 			        
			        	html: 'Wordgame'
			        },
			        { xtype: 'spacer' },
			        {
			        	itemId: 'showConnecting',
			        	html: 'connecting...'
			        },		
			        {
			        	itemId: 'showConnected',
			        	hidden: true,
			        	html: ''
			        },
			        {
			        	itemId: 'showWaitOpponent',
			        	hidden: true,
			        	html: 'Hold on while you opponent joins...'
			        },
			        {
			        	itemId: 'showOpponent',
			        	hidden: true,
			        	html: 'Your opponent joined.'
			        },
			        {
			        	html: 'Yout opponent is ready to start',
			        	itemId: 'opponent-started',
			        	hidden: true
			        },
			        { xtype: 'spacer' },
			        {
			        	itemId: 'pressStart',
			        	hidden: true,
			        	html: 'Press PLAY when you are ready'
			        },
			        { xtype: 'spacer' },
			        {
			        	xtype: 'button',
			        	itemId: 'start-button',
			        	disabled: true,
			        	text: "PLAY",
			        	handler: function() {
			        		console.log('[View.Start] #start-button.handler');
			        		App.fireEvent('player-button-start');
			        	}
			        },			       
			        {
			        	html: 'Waiting for opponent...',
			        	itemId: 'startSpinner',
			        	hidden: true
			        },
			        {
			        	html: 'Game On!',
			        	itemId: 'game-on',
			        	hidden: true
			        },
			        { xtype: 'spacer'}
			        
			]		
		}
		Ext.apply(this, config);
		App.View.Start.superclass.initComponent.call(this);
	},
	
	showPlayerConnected: function(game) {
		console.log("[View.Start] player connected");
		this.query('#showConnecting')[0].hide();
		this.query('#showConnected')[0].show();
		this.query('#showWaitOpponent')[0].show();
	},
	
	showPlayerStarted: function(game) {
		console.log('[View.Start] show player started');
		this.query('#startSpinner')[0].show();
		this.query('#start-button')[0].disable();
		this.query('#start-button')[0].setText('Starting...');
	},
	
	showOpponentConnected: function(game) {
		console.log('[View.Start] opponent connected');
		this.query('#pressStart')[0].show();
		this.query('#showWaitOpponent')[0].hide();
		this.query('#showOpponent')[0].show();
		this.query('#start-button')[0].enable();
	},
	
	showOpponentStarted: function(game) {
		console.log('[View.Start] opponent started');
		this.query('#startSpinner')[0].hide();
		this.query('#opponent-started')[0].show();
	},
	
	showGameOn: function() {
		console.log('[View.Start] game on');
		this.query('#game-on')[0].show();
	}
	
});
Ext.reg('App.View.Start', App.View.Start);

//