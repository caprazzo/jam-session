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