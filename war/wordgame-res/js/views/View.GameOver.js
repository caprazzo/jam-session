// View.Game.js
Ext.ns('App.View');

App.View.GameOver = Ext.extend(Ext.Panel, {
	
	initComponent: function() {
		console.log("[View.GameOver] initComponent");
		var config = {
			layout: 'vbox',
			items: [
			        {xtype: 'spacer'},
			        { 			        
			        	html: 'Game Over'
			        },
			        {
			        	itemId: 'playerWin',
			        	html: 'You WIN!',
			        	hidden: true
			        },
			        {
			        	itemId: 'opponentWin',
			        	html: 'You LOSE!',
			        	hidden: true
			        },
			        { xtype: 'spacer' },
			        {
			        	itemId: 'player-confirm-replay-request',
			        	xtype: 'button',
			        	html: 'Opponent wants to replay. Tap to replay.',
			        	hidden: true,
			        	handler: function() {
			        		console.log('[View.GameOver] #player-confirm-replay-request.handler');
			        		App.fireEvent('player-replay-button');
			        	}
			        },
			        {
			        	itemId: 'player-replay-button',
			        	xtype: 'button',
			        	html: 'Play again',
			        	hidden: true,
			        	handler: function() {
			        		console.log('[View.GameOver] #player-replay-button.handler');
			        		App.fireEvent('player-replay-button');
			        	}
			        },
			        {
			        	itemId: 'replay-spinner',
			        	html: 'Wait for opponent to accept...',
			        	hidden: true
			        },
			        { xtype: 'spacer' }
			]		
		}
		Ext.apply(this, config);
		App.View.GameOver.superclass.initComponent.call(this);
		
		console.log("[View.GameOver] component ready");
	},
	
	showPlayerWin: function() {
		this.query('#playerWin')[0].show();
		this.query('#player-replay-button')[0].show();
	},
	
	showOpponentWin: function() {
		this.query('#opponentWin')[0].show();
		this.query('#player-replay-button')[0].show();
	},
	
	showPlayerRequestReplay: function() {
		this.query('#player-replay-button')[0].disable();
		this.query('#replay-spinner')[0].show();
	},
	
	showOpponentRequestReplay: function() {
		this.query('#player-replay-button')[0].hide();
		this.query('#replay-spinner')[0].hide();
		this.query('#player-confirm-replay-request')[0].show();
	}

});
Ext.reg('App.View.GameOver', App.View.GameOver);