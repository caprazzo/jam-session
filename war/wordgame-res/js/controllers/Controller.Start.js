
// Controller.Start.js
Ext.regController('Start', {
	
	index: function(options) {
		console.log('[Controller.Start] index');

		if (options.game && !this.game)
			this.initGame(options.game);
		
		// this user ready to play
		App.on(
			'player-button-start',
			function() {
				console.log('Controller.Start pressed button start');
				this.game.playerStart();
			},
			this
		);
		
		this.showStart();
	},	
	
	initGame: function(game) {
		console.log('[Controller.Start] initGame');
		this.game = game;
		
		this.game.on(
			'player-connected',
			function() {
				console.log('[Controller.Start] game.on.player-connected');
				this.viewStart.showPlayerConnected(this.game);
			},
			this
		);
		
		this.game.on(
			'player-started',
			function() {
				console.log('[Controller.Start] game.on.player-started');
				this.viewStart.showPlayerStarted();
			},
			this
		);
		
		this.game.on(
			'opponent-connected',
			function() {
				console.log('[Controller.Start] game.on.opponent-connected');
				this.viewStart.showOpponentConnected(this.game);
			},
			this
		);
		
		this.game.on(
			'opponent-started',
			function(game) {
				console.log('[Controller.Start] game.on.opponent-started', game);
				this.viewStart.showOpponentStarted();
			},
			this
		);
		
		this.game.on(
			'game-on',
			function() {
				console.log('[Controller.Start] game.on.opponent-started', game);
				this.viewStart.showGameOn();
			},
			this
		);
		
		this.game.connect();
	},
	
	showStart: function() {
		console.log('[Controller.Start] showStart');
		if (!this.viewStart) {

			this.viewStart = this.render({
				xtype: 'App.View.Start'
			});
			
			/* example bind tap:
			this.viewChat.query('#settingsButton')[0].on(
				'tap',
				this.showConfig,
				this
			);
			*/
		}
		
		/* example set active item:
		this.application.viewport.setActiveItem(
			this.viewStart,
			{
				type: 'slide',
				direction: 'left'
			}
		);
		*/
	},
	
	showGame: function() {
		Ext.dispatch({
			controller: 'Viewport',
			action    : 'showGame'
		});
	}
	
});