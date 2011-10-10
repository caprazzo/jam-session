
//start ./js/index.js

Ext.regApplication('App', {
	defaultTarget: 'viewport',
	defaultUrl   : 'Viewport/index',
	name         : 'App',
	useHistory   : false,
	useLoadMask : true,

	launch: function() {
		
		Ext.Viewport.init();
		Ext.Viewport.onOrientationChange();
								
		var token_el = document.getElementById('CHANNEL_TOKEN');
		var token = token_el.value;
		
		var post_url_el = document.getElementById('POST_URL');
		var post_url = post_url_el.value;
		
		var words_el = document.getElementById('GAME_WORDS');
		var words = words_el.value.split(',');
		 // words = ['AB','CD']; 
		// words = ['AB','CAPRAZZI', 'ROX', 'MARTELLA', 'SUX'];
		
		var client = new App.util.GameClient({channel_token:token, post_url:post_url });
		var game = new App.util.Game({ words: words, client:client });
		
		this.viewport = new App.View.Viewport({
			application: this
		});		

		console.log('[App.launch] invoking Viewport.index');
		Ext.dispatch({
			controller: 'Viewport',
			action    : 'index',
			game:	game
		});
	}
});


//end ./js/index.js


//start ./js/lib/App.util.Game.js

Ext.ns('App.util');
App.util.Game = Ext.extend(Ext.util.Observable, {

	constructor: function(options) {
		console.log('[util.Game] constructor', options);
		
		App.util.GameClient.superclass.constructor.call(
			this
		);
		
		this.initWords(options.words);
		this.initClient(options.client);
		
		this.player = {
			progress: 0,
			connected: false,
			completed: false,
			win: false,
			request_replay: false,
			accept_replay: false
		};
		
		this.opponent = {
			progress: 0,
			connected: false,
			completed: false,
			win: false,
			request_replay: false,
			accept_replay: false
		};
	},
	
	initWords: function(words) {
		console.log('[util.Game] initWords', words);
		this.words = words;
		this.current_word = -1;
		this.letter_count = this.words.join('').length;
		this.letters_picked = 0;
		
		function case_insensitive_comp(strA, strB) {
		    return strA.toLowerCase().localeCompare(strB.toLowerCase());
		}
		
		function sortWord(word) {
			word = word.split('');
			
			word = word.sort();
			word = word.sort(case_insensitive_comp);
			return word.join('');
		}
		
		this.words_sorted = [];
		for(var i=0; i < words.length;i++) {
			this.words_sorted.push(sortWord(words[i]));
		}
		
		console.log('[util.Game] sorted words', this.words_sorted);
		
		console.log('[util.Game] words initialized', this);
		
		this.words_picked = [];
	},
	
	initClient: function(client) {
		console.log('[util.Game] initClient', client);
		this.client = client;
		
		// attach handlers to client events
		
		this.client.on(
			'player-connected',
			function() {
				console.log('[util.Game] client.on.player-connected');
				this.player.connected = true;
				this.fireEvent('player-connected');
			},
			this
		);
		
		this.client.on(
			'player-started',
			function() {
				console.log('[util.Game] client.on.player-started');
				this.player.started = true;
				this.fireEvent('player-started');
				
				if (this.opponent.started) {
					this.fireEvent('game-on');
				}
			},
			this
		);
		
		this.client.on(
			'player-request-replay',
			function() {
				console.log('[util.Game] client.on.player-request-replay');
				this.player.request_replay = true;
				this.fireEvent('player-request-replay');
			},
			this
		);
		
		this.client.on(
			'player-accept-replay',
			function() {
				console.log('[util.Game] client.on.player-accept-replay');
				this.player.accept_replay = true;
				this.fireEvent('player-accept-replay');
			},
			this
		)
		
		this.client.on(
			'opponent-request-replay',
			function() {
				console.log('[util.Game] client.on.opponent-request-replay');
				this.opponent.request_replay = true;
				if (this.player.request_replay) {
					this.client.playerAcceptReplay();
					return;
				}
				this.fireEvent('opponent-request-replay');
			},
			this
		);
		
		this.client.on(
			'opponent-connected',
			function(opponent) {
				console.log('[util.Game] client.on.opponent-connected')
				this.opponent.connected = true;
				this.opponent.name = opponent.name;
				this.fireEvent('opponent-connected');
			},
			this
		);
		
		this.client.on(
			'opponent-started',
			function() {
				console.log('[util.Game] client.on.opponent-started');
				this.opponent.started = true;
				this.fireEvent('opponent-started');
				
				if (this.player.started) {
					this.fireEvent('game-on');
				}
			},
			this
		);
		
		this.client.on(
			'opponent-win',
			function() {
				console.log('[util.Game] client.on.opponent-win');
				this.opponent.completed = true;
				this.opponent.win = true;
				this.fireEvent('opponent-win');
			},
			this
		);
		
		this.client.on(
			'player-win',
			function() {
				console.log('[util.Game] client.on.player-win');
				this.player.win = true;
				this.fireEvent('player-win');
			},
			this
		);
		
		this.client.on(
			'opponent-update-progress',
			function(pct) {
				console.log('[util.Game] client.on.opponent-update-progress');
				this.opponent.progress = pct;
				this.fireEvent('opponent-update-progress', pct);
			},
			this
		);
	},
	
	/**** methods exposed to application ****/
	
	// connect to server
	connect: function() {
		console.log('[util.Game] connect');
		this.client.connect();
	},
	
	// user pressed button start
	playerStart: function() {
		console.log('[util.Game] playerStart');
		this.client.playerStart();
	},
	
	// start game
	startGame: function() {
		console.log('[util.Game] startGame');
		this.nextWord();
	},
	
	playerRequestReplay: function() {
		console.log('[util.Game] playerRequestReplay');
		this.client.playerRequestReplay();
	},
	
	nextWord: function() {
		console.log('[util.Game] nextWord');
		if (this.isGameComplete()) {
			console.log('[util.Game] no more words: game complete');
			return;
		}
		this.current_word += 1;
		this.words_picked.push(0);
		this.fireEvent('next-word');
	},
	
	pickLetter: function(n_letter) {
		console.log('[util.Game] pickLetter ', n_letter);
		
		var word = this.getCurrentWord();
		var sorted = this.getCurrentWordSorted();
		var picked = this.getCurrentWordPicked();
		var letter = word[n_letter];
		var correct = (letter == sorted[picked]);
		
		if (!correct) {
			this.pickedKo();
			return;
		}
		
		this.pickedOk(n_letter);
		
		if (this.isGameComplete()) {
			this.gameComplete();
			return;
		}
		
		if (this.isCurrentWordComplete()) {
			this.wordComplete();
			return;
		}
	},
	
	/**** private methods ****/
	
	getCurrentWord: function() {
		return this.words[this.current_word];
	},
	
	getCurrentWordSorted: function() {
		return this.words_sorted[this.current_word];
	},
	
	getCurrentWordPicked: function() {
		return this.words_picked[this.current_word];
	},
	
	isCurrentWordComplete: function() {
		return (this.getCurrentWordPicked() == this.getCurrentWord().length);
	},
	
	isGameComplete: function() {
		return this.isGameStarted() && (this.isCurrentWordComplete() && this.current_word == this.words.length - 1);
	},
	
	isGameStarted: function() {
		return this.current_word >= 0;
	},
	
	updatePlayerProgress: function() {
		this.player.progress = (100 / this.letter_count) * this.letters_picked;
		this.fireEvent('player-update-progress', this.player.progress);
		this.client.updatePlayerProgress(this.player.progress);
	},
	
	setPickedCorrect: function() {
		this.letters_picked += 1;
		this.words_picked[this.current_word] += 1;
	},
	
	pickedOk: function(n_letter) {
		console.log('[util.Game] picked ok', n_letter);
		this.setPickedCorrect();
		this.fireEvent('player-picked-ok', n_letter);
		this.updatePlayerProgress();
	},
	
	pickedKo: function(n_letter) {
		console.log('[util.Game] picked ko', n_letter);
		this.fireEvent('player-picked-ko', n_letter);
	},
	
	gameComplete: function() {
		console.log('[util.Game] player game complete');
		this.player.completed = true;
		this.fireEvent('player-game-completed');
		this.client.playerGameCompleted();
	},
	
	wordComplete: function() {
		console.log('[util.Game] word completed', this.getCurrentWord(), this.current_word);
		this.fireEvent('player-word-completed', this.current_word);
	}
	
});
Ext.reg('App.util.Game', App.util.Game);
//end ./js/lib/App.util.Game.js


//start ./js/lib/App.util.GameClient.js

Ext.ns('App.util');
/**
 * Socket.io  wrapper class
 * @class App.util.ChatClient
 * @extends Ext.util.Observable
 * 
 * Events:
 * 		'player-connected': when this client is connected to the server
 */
App.util.GameClient = Ext.extend(Ext.util.Observable, {

	constructor: function(options){
		console.log('[util.GameClient] constructor ', options)
		
		options = options || {};
		this.throttle_ms = options.throttle_ms || 5000;

		App.util.GameClient.superclass.constructor.call(
			this
		);

		this.channel_token = options.channel_token;
		this.post_url = options.post_url;
		this.socket = null;		
		this.update_timer = 0;
	},

	/**
	 * connect
	 */
	connect: function() {
		console.log('[util.GameClient] connect');
		var channel = new goog.appengine.Channel(this.channel_token);
		var that = this;
		this.socket = channel.open({
	    	onopen: function() {
	    		console.log('[util.GameClient] socket.onopen');
	    		that.fireEvent('player-connected');   	 
		    },
		    onmessage: function(msg) {
		    	console.log('[util.GameClient] socket.onmessage', msg);
		    	that.processServerMessage(Ext.util.JSON.decode(msg.data));
		    },
		    onerror: function(err) {
		    	console.log(err);
		    },
		    onclose: function() {
		    	console.log('channel closed');
		    	that.onDisconnect();
		    }
	    });
		console.log('[util.GameClient] channel opened');
	},
	
	send: function(data, callback) {
		console.log(data + ' [send-begin]');
		Ext.Ajax.request({
			method: 'POST',
			url: this.post_url,
			success: function() {
				console.log(data + ' [send-success]');
				if (callback)
					callback();
			},
			failure: function() {
				console.log(data + ' [send-failure]');
			},
			params: data
		});
		console.log(data + ' [send-invoked]');
	},
	
	processServerMessage: function(message) {		
		console.log('[util.GameClient] rcv message', message);
		// other player joined
		if (message.cmd == 'opponent-connected') {
			this.fireEvent('opponent-connected', { name: message.name });
			return;
		}
		
		// other player pressed start button
		if (message.cmd == 'opponent-started') {
			this.fireEvent('opponent-started');
			return;
		}
		
		if (message.cmd == 'player-win') {
			this.fireEvent('player-win');
			return;
		}
		
		if (message.cmd == 'opponent-win') {
			this.fireEvent('opponent-win');
			return;
		}
		
		if (message.cmd == 'opponent-update-progress') {
			this.fireEvent('opponent-update-progress', message.progress);
			return;
		}
		
		if (message.cmd == 'opponent-request-replay') {
			this.fireEvent('opponent-request-replay');
			return;
		}
		
		if (message.cmd == 'redirect') {
			window.location = message.location;
			return;
		}
		
		if (message.cmd == 'leave') {
			this.onOpponentLeave();
			return;
		}
	},
	
	playerStart: function() {
		console.log('[util.GameClient] playerStart');
		this.update_timer = new Date().getTime();
		var that = this;
		this.send({cmd:'start'}, function() {
			console.log('[util.GameClient] playerStart.success');
			that.fireEvent('player-started');
		});
	},
	
	playerRequestReplay: function() {
		console.log('[util.GameClient] playerRequestReplay');
		var that = this;
		this.send({cmd: 'player-request-replay'}, function() {
			console.log('[util.GameClient] playerRequestReplay.success');
			that.fireEvent('player-request-replay');
		});
	},
	
	playerAcceptReplay: function() {
		console.log('[util.GameClient] playerAcceptReplay');
		var that = this;
		this.send({cmd: 'player-accept-replay'}, function() {
			console.log('[util.GameClient] playerAcceptReplay.success');
			that.fireEvent('player-accept-replay');
		});
	},
	
	playerGameCompleted: function() {
		console.log('[util.GameClient] playerGameCompleted sending');
		var that = this;
		this.send({cmd: 'player-completed'}, function() {
			console.log('[util.GameClient] playerGameCompleted.success');
		});
	},
	
	updatePlayerProgress: function(pct) {
		var update_elapsed = ((new Date().getTime()) - this.update_timer);
		if (update_elapsed < this.throttle_ms) {
			console.log('[util.GameClient] updatePlayerProgress skipping', update_elapsed);
			return;
		}
		this.update_timer = new Date().getTime();
		console.log('[util.GameClient] updatePlayerProgress');
		var that = this;
		this.send({cmd: 'player-update-progress', progress: pct}, function() {
			console.log('[util.GameClient] updatePlayerProgress.success');
		});
	},
	
	disconnect: function(){
		if (this.socket != null)
			this.socket.close();
	}

});
Ext.reg('App.util.GameClient', App.util.GameClient);
//end ./js/lib/App.util.GameClient.js


//start ./js/controllers/Controller.Game.js


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
	
	
//end ./js/controllers/Controller.Game.js


//start ./js/controllers/Controller.GameOver.js

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
//end ./js/controllers/Controller.GameOver.js


//start ./js/controllers/Controller.Start.js


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
//end ./js/controllers/Controller.Start.js


//start ./js/controllers/Controller.Viewport.js


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
//end ./js/controllers/Controller.Viewport.js


//start ./js/models/Model.Player.js

// screen name
// score

//end ./js/models/Model.Player.js


//start ./js/models/Model.Game.js

// word list
// stats
//end ./js/models/Model.Game.js


//start ./js/views/View.GameWord.js


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

//end ./js/views/View.GameWord.js


//start ./js/views/View.Game.js


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
//end ./js/views/View.Game.js


//start ./js/views/View.GameOver.js

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
//end ./js/views/View.GameOver.js


//start ./js/views/View.Start.js

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
//end ./js/views/View.Start.js


//start ./js/views/View.Viewport.js

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
//end ./js/views/View.Viewport.js

