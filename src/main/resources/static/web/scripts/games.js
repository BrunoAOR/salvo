var $gamesList;

$(function() {
    $gamesList = $('#games-list');

    $.getJSON('/api/games', onDataReady);
})

function onDataReady(games) {
    for (var i = 0; i < games.length; ++i) {
        $gamesList.append(getGameRow(games[i]));
    }
}

function getGameRow(game) {
    var $li = $(document.createElement("li"));
	var content = "";
	content += new Date(game.created).toLocaleString() + ": ";
	content += game.gamePlayers[0].player.email + ", ";
	content += game.gamePlayers[1].player.email;
	$li.text(content);
	console.log(content);
	return $li;
}