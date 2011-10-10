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

