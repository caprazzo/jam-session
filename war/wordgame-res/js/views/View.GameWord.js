
// deprecated
// View.Game.js
Ext.ns('App.View');

App.View.GameWord = Ext.extend(Ext.Panel, {
	
	initComponent: function(word) {
		console.log("[View.GameWord] initComponent");
		var config = {
			layout: 'hbox',
			items: []		
		}
		
		Ext.apply(this, config);
		App.View.GameWord.superclass.initComponent.call(this);
		console.log("[View.GameWord] component ready");	
	},
	
	setWord: function(word) {
		
		this.buttons = [];
		console.log("[View.GameWord] setWord " + word);
		
		var that = this;
		word = word.split('');
		for (var i=0; i<word.length; i++) {
			
			function handle(el) {
				var n_letter = el.n_letter;
				console.log('[View.GameWord] tap letter ' + n_letter + '(' + word[n_letter] + ')')
				that.fireEvent('pick-letter', n_letter);
			}
			
			var button = { 
				xtype: 'button',
				text: word[i],
				itemId: 'letter' + i,
				handler: handle,
				n_letter: i
			};
			
			this.add(button);
			this.buttons.push(this.query('#letter'+i)[0]);
		}
		this.doLayout();
	},
	
	showPickedKo: function(n_letter) {
		console.log("[View.GameWord] showPickedKo", n_letter);
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
		console.log("[View.GameWord] showPickedOk", n_letter);
		this.buttons[n_letter].disable();
		this.buttons[n_letter] = null;
	}
});
Ext.reg('App.View.GameWord', App.View.GameWord);
