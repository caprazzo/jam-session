
// View.Game.js
Ext.ns('App.View');

App.View.Game = Ext.extend(Ext.Panel, {
	
	initComponent: function() {
		console.log("[View.Game] initComponent");
		var config = {
			layout: 'vbox',
			items: [
			        { 			        
			        	html: 'Play Wordgame'
			        },
			        {
			        	html: 'Tap the letters in alphabetical order'
			        },
			        { xtype: 'spacer' },
			        {
			        	xtype: 'panel',
			        	align: 'stretch',
			        	itemId: 'current-word',
			        	layout: 'hbox',
			        	items: []
			        },
			        { xtype: 'spacer' },
			        
			        {
			        	xtype: 'panel',
			        	layout: 'vbox',
			        	width: 250,
			        	style: {
			        		border: '1px solid black'
			        	},
			        	items: [
			        
			        {
			        	xtype: 'panel',
			        	layout: 'hbox',
			        	width: 250,
			        	items: [
			        	    { 
					        	itemId: 'player-progress',
					        	html: '&nbsp;',
					        	style: {			        		
					        		width: '10',
					        		backgroundColor: 'green',
					        		color: 'black',
					        		align: 'left'
					        	}
					        },
			        	    { 
					        	html: 'you',
					        	backgroundColor: 'green',
					        	color: 'white'
					        },
			        	]
			        },
			        {
			        	xtype: 'panel',
			        	layout: 'hbox',
			        	width: 250,
			        	items: [
			        	    { 
					        	itemId: 'opponent-progress',
					        	html: '&nbsp;',
					        	style: {			        		
					        		width: '10',
					        		backgroundColor: 'red',
					        		color: 'black',
					        		align: 'left'
					        	}
					        },
			        	    { 
					        	html: 'opponent',
					        	backgroundColor: 'red',
					        	color: 'white'
					        },
			        	]
			        }]},
			        { xtype: 'spacer' }
			]		
		};
		
		Ext.apply(this, config);
		App.View.Game.superclass.initComponent.call(this);
		
		this.wordPanel = this.query('#current-word')[0];
		// notify to client that ui is ready
		console.log("[View.Game] component ready");
		
		var that = this;
		setTimeout(function() {
			//this.on('ready', function() {
			//	console.log('[View.Game] trigger-read');
			//}, this);
			that.fireEvent('ready');
		}, 1000);
		
		
	},
	
	setWord: function(word) {
		
		while(f = this.wordPanel.items.first()) {
		  this.wordPanel.remove(f, true);
		}
		
		this.buttons = [];
		console.log("[View.Game] setWord " + word);
		
		var that = this;
		word = word.split('');
		for (var i=0; i<word.length; i++) {
			
			function handle(el) {
				var n_letter = el.n_letter;
				console.log('[View.Game] tap letter ' + n_letter + '(' + word[n_letter] + ')')
				that.fireEvent('pick-letter', n_letter);
			}
			
			var button = { 
				xtype: 'button',
				text: word[i],
				itemId: 'letter' + i,
				handler: handle,
				n_letter: i,
				margin: 5
			};
			
			this.wordPanel.add(button);
			this.buttons.push(this.query('#letter'+i)[0]);
		}
		this.doLayout();
	},
	
	showPickedKo: function(n_letter) {
		console.log("[View.Game] showPickedKo", n_letter);
		for(var i=0; i<this.buttons.length; i++) {
			var btn = this.buttons[i];
			if (btn != null) {
				btn.disable();
			}
		}
		var that = this;
		setTimeout(function() {
			for(var i=0; i<that.buttons.length; i++) {
				var btn = that.buttons[i];
				if (btn != null) {
					btn.enable();
				}
			}
		}, 500);
	},
	
	showPickedOk: function(n_letter) {
		console.log("[View.Game] showPickedOk", n_letter);
		this.buttons[n_letter].disable();
		this.buttons[n_letter] = null;
	},
	
	updatePlayerProgress: function(pct) {
		console.log("[View.Game] updatePlayerProgress", pct);
		this.query('#player-progress')[0].setWidth(pct * 2.5);
	},
	
	updateOpponentProgress: function(pct) {
		console.log("[View.Game] updateOpponentProgress", pct);
		this.query('#opponent-progress')[0].setWidth(pct * 2.5);
	}
	
	
});
Ext.reg('App.View.Game', App.View.Game);