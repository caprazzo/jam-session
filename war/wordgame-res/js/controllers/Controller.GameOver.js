Ext.regController('GameOver', {
	
	index: function(options) {
		console.log('[Controller.GameOver] index', options);	
		
		if (options.game)
			this.initGame(options.game);
		
		this.initView();
		
		App.on(
				'player-replay-button',
				function() {
					console.log('[Controller.GameOver] pressed replay button');
					this.game.playerRequestReplay();
				},
				this
			);
	},
	
	initGame: function(game) {
		this.game = game;
		
		this.game.on(
			'player-win',
			function() {
				this.viewGameOver.showPlayerWin();
			},
			this
		);
		
		this.game.on(
			'player-request-replay',
			function() {
				this.viewGameOver.showPlayerRequestReplay();
			},
			this
		);
		
		this.game.on(
			'opponent-request-replay',
			function() {
				this.viewGameOver.showOpponentRequestReplay();
			},
			this
		)
		
		this.game.on(
			'opponent-win',
			function() {
				this.viewGameOver.showOpponentWin();
			},
			this
		);
	},
	
	initView: function() {
		console.log('[Controller.GameOver] initView');
		if (!this.viewGameOver) {
			this.viewGameOver = this.render({
				xtype: 'App.View.GameOver'
			});
			
			if (this.game.player.win) {
				this.viewGameOver.showPlayerWin();
			}
			else if (this.game.opponent.win) {
				this.viewGameOver.showOpponentWin();
			}
		}
	}
});