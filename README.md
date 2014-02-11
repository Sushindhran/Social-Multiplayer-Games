Social-Multiplayer-Games
========================

Social Multiplayer Games repository - NYU, Spring 2014

SCRABBLE - Game Description
---------------------------

Representing the states of the game using Game API

https://docs.google.com/a/nyu.edu/presentation/d/1f_1qH329XWnjZKyXLQktrfTqhsKVBBlAWl2_0SOYUzU/edit#slide=id.p

Refer to http://en.wikipedia.org/wiki/Scrabble

•The game can have 2-4 players. Let us assume that there are only two players for the sake of simplicity.

•There is a sack with 100 tiles, each tile is a letter. Each letter has a unique score. Refer to the following link for the tile distribution. http://en.wikipedia.org/wiki/Scrabble_letter_distributions#English

•Each player has a rack where he can place the tiles. Before the first move, each player takes 7 tiles without looking and places them on his/her rack. These tiles are not revealed to the opponent.

•The game starts off with the first player making his first move by making a word from his rack that has one tile on the pink square(star) in the middle of the board. All tiles placed on the board in one turn have to be in a straight line either vertically or horizontally. Also, all words have to be oriented left-right or top-down.

•A player can challenge the opponent’s move before making his move and if the word is invalid, the opponent has to take back all his tiles. The opponent’s turn is lost and he scores no points. Otherwise the challenger loses his turn and the opponent makes his next move.

•The score for a turn is calculated by counting the points on individual tiles placed on the board. If the new word conjoins with a word already on the board, the score is calculated by adding the points for the conjoined words as well. There can be any number of valid conjoined words.

•There are several coloured squares on the board which affect the score of a word. Pink squares double the points of a word and red squares treble them. Light blue squares double the points for a letter placed on it and dark blue square treble them. If a letter that is placed on one of these squares conjoins with another valid word already on the board, the qualities of that square is applied to the conjoining word as well.

•If a player uses all 7 tiles on his rack in one turn, he makes a Bingo and gets a 50 point bonus. If he uses only 4 tiles, he picks 4 new ones without looking, thus maintaining 7 tiles on his rack at all times.

•A player can also pass a turn or exchange tiles on his rack. If he exchanges, he has to forgo his turn. Exchanges can be made only if there are at least 7 tiles left in the bag.

•The game ends when all the tiles are exhausted or if no new valid words can be made. If there are any tiles left on the rack, the points of all the letters are summed up, doubled and then added to the opponents score.

