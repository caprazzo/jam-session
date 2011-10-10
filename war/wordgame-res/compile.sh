
files='
	./js/index.js

	./js/lib/App.util.Game.js
 	./js/lib/App.util.GameClient.js


	./js/controllers/Controller.Game.js
	./js/controllers/Controller.GameOver.js
 	./js/controllers/Controller.Start.js
	./js/controllers/Controller.Viewport.js

	./js/models/Model.Player.js
	./js/models/Model.Game.js

	./js/views/View.GameWord.js
	./js/views/View.Game.js
	./js/views/View.GameOver.js
	./js/views/View.Start.js
	./js/views/View.Viewport.js	
'

rm all.js
for f in $files; do
	echo "\n//start $f\n" >> all.js
	cat $f >> all.js
	echo "\n//end $f\n" >> all.js
done