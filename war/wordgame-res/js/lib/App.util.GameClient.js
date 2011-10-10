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