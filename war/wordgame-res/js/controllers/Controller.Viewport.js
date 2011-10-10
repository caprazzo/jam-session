
Ext.regController('Viewport', {
			
	index: function(options) {
		console.log('[Controller.Viewport] index', options);
		this.game = options.game;
		
		this.game.on(
			'game-on',
			function() {
				console.log('[Controller.Viewport] game.on.game.on');
				this.showGame();
			},
			this
		);
		
		this.game.on(
			'player-game-completed',
			function() {
				console.log('[Controller.Game] game.on.game-completed');
				this.showGameOver();
			},
			this
		);
		
		this.game.on(
			'opponent-win',
			function() {
				this.showGameOver();
			},
			this
		);
		
		this.showStart();
	},
	
	showStart: function() {
		console.log('[Controller.Viewport] showStart');
		Ext.dispatch({
			controller: 'Start',
			action    : 'index',
			game: this.game
		});
	},
	
	showGame: function() {
		console.log('[Controller.Viewport] showGame');
		Ext.dispatch({
			controller: 'Game',
			action: 'index',
			game: this.game
		});
	},
	
	showGameOver: function() {
		console.log('[Controller.Viewport] showGameOver');
		Ext.dispatch({
			controller: 'GameOver',
			action: 'index',
			game: this.game
		});
	}

});