
Ext.regController('Game', {
	
	index: function(options) {
		console.log('[Controller.Game] index', options);	
		
		if (options.game)
			this.initGame(options.game);
		
		this.initView();
	},
	
	initGame: function(game) {
		this.game = game;
		
		this.game.on(
			'next-word',
			function() {
				this.viewGame.setWord(this.game.getCurrentWord());
			},
			this
		);
		
		this.game.on(
			'player-picked-ko',
			function(n_letter) {
				console.log('[Controller.Game] game.on.picked-ko', n_letter);
				this.viewGame.showPickedKo(n_letter);
			},
			this
		);
		
		this.game.on(
			'player-picked-ok',
			function(n_letter) {
				console.log('[Controller.Game] game.on.picked-ok', n_letter);
				this.viewGame.showPickedOk(n_letter);
			},
			this
		);
		
		this.game.on(
			'player-word-completed',
			function(n_word) {
				console.log('[Controller.Game] game.on.word-completed', n_word);
				this.game.nextWord();
			},
			this
		);
		
		this.game.on(
			'player-update-progress',
			function(pct) {
				console.log('[Controller.Game] game.on.player-update-progress', pct);
				this.viewGame.updatePlayerProgress(pct);
			},
			this
		);
		
		this.game.on(
			'opponent-update-progress',
			function(pct) {
				console.log('[Controller.Game] game.on.opponent-update-progress', pct);
				this.viewGame.updateOpponentProgress(pct);
			},
			this
		);
	},
	
	initView: function() {
		console.log('[Controller.Game] initView');
		if (!this.viewGame) {
			
			this.viewGame = this.render({
				xtype: 'App.View.Game'
			});
			
			this.viewGame.on(
				'ready',
				function() {
					console.log('[Controller.Game] ViewGame.on.ready');
					this.game.startGame();
					this.update_timer = new Date().getTime();
				},
				this
			);
			
			this.viewGame.on(
				'pick-letter',
				function(n_letter) {
					console.log('[Controller.Game] wordView.on.pick-letter', n_letter);
					this.game.pickLetter(n_letter);
				},
				this
			);
		}
	},
	
	showGame: function() {
		console.log('[Controller.Game] showGame');
	}
	
});
	
	