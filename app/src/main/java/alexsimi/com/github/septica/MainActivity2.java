package alexsimi.com.github.septica;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

import alexsimi.com.github.septica.model.Card;
import alexsimi.com.github.septica.model.Rank;
import alexsimi.com.github.septica.model.Suit;

public class MainActivity2 extends AppCompatActivity {
    //fields layout
    private ImageView[] computerCardsImageView;
    private ImageView[] humanCardsImageView;
    private ImageView[] centerCardsImageView;
    private Button foldButton;
    private TextView computerPointsView;
    private TextView humanPointsView;
    private TextView computerWinMessage;
    private TextView humanWinMessage;
    private ImageView turnBallComputer;
    private ImageView turnBallHuman;
    private TextView computerSetPoints;
    private TextView humanSetPoints;

    //fields deck
    private ArrayList<Card> deck;
    private ArrayList<Card> humanCards;
    private ArrayList<Card> computerCards;
    private ArrayList<Card> centerCards;

    //fields other
    private boolean blockCards;
    private boolean isHumanWhoStartedHand;
    private boolean isHumanNext;
    private int humanPoints;
    private int computerPoints;
    private int humanGeneralScore;
    private int computerGeneralScore;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //first setup
        createDecks();
        initializeCards();
        setupHand();
        setupHandUI();

        //decide first player
        int randomN = (int) (Math.random() * 10 % 2);
        if (randomN == 0) //computer first serve
        {
            turnBallHuman.setVisibility(View.INVISIBLE);
            isHumanWhoStartedHand = false;
            isHumanNext = false;
            foldButton.setEnabled(false);
            computerMove();
        }
        else//human first serve
        {
            turnBallComputer.setVisibility(View.INVISIBLE);
            isHumanWhoStartedHand = true;
            isHumanNext = true;
            foldButton.setEnabled(false);
        }

    }

    public void humanMove(View v) {
        if (!isHumanNext || blockCards)
            return;
        //search for card clicked in humanCards

        int k = -1;
        for (int i = 0; i < humanCardsImageView.length; ++i) {
            if (v.getId() == humanCardsImageView[i].getId()) {
                k = i;
            }
        }

        //modify centerCards
        centerCards.add(humanCards.remove(k));
        humanMoveUI();

        //make computer move
        isHumanNext = false;
        computerMove();


    }

    public void humanMoveUI() {

        turnBallHuman.setVisibility(View.INVISIBLE);
        turnBallComputer.setVisibility(View.VISIBLE);

        //human setup
        for (int i = 0; i < humanCards.size(); ++i) {

            humanCardsImageView[i].setImageResource(humanCards.get(i).getResourceId());
        }

        if (humanCards.size() < 4) {
            for (int i = humanCards.size(); i < 4; ++i) {
                humanCardsImageView[i].setVisibility(View.INVISIBLE);
            }
        }

        //center setup
        for (int i = 0; i < centerCards.size(); ++i) {
            centerCardsImageView[i].setVisibility(View.VISIBLE);
            centerCardsImageView[i].setImageResource(centerCards.get(i).getResourceId());
        }
    }

    public void computerMove()
    {
        if(!isHumanWhoStartedHand && centerCards.size() <= 0)
        {
            //computer is serving and center cards is empty
            //computerSimpleMove();

            Thread aThread = new Thread(new Runnable() {
                @Override
                public void run()
                {
                    blockCards = true;
                    SystemClock.sleep(1000);
                    computerSimpleMove();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            computerMoveUI();
                            blockCards = false;
                        }
                    });
                }

            });
            aThread.start();
        }
        else if(!isHumanWhoStartedHand && centerCards.size() > 0)//computer is serving and not the first computer move
        {
            computerComplexMove();
        }
        else // human is serving
        {
            computerSimpleMove();

            Thread aThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    blockCards = true;
                    SystemClock.sleep(1000);

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            computerMoveUI();

                            //search for cards to cut with, in human cards
                            boolean cantCut = false;
                            for (int i = 0; i < humanCards.size(); ++i)
                            {
                                if ((humanCards.get(i).getValue() == centerCards.get(0).getValue()) || (humanCards.get(i).getValue() == 7))
                                {
                                    cantCut = true;
                                    break;
                                }
                            }
                            blockCards = false;
                            //if no cards to cut with, fold
                            if (!cantCut)
                                decideWinner(foldButton);
                        }
                    });
                }

            });
            aThread.start();


        }

            //if there are 8 cards in center and human started the hand, automatic fold
            //or if it is the last hand of the game
//            if ((isHumanWhoStartedHand && centerCards.size() == 8) || (deck.size() == 0 && computerCards.size() == 0 && isHumanWhoStartedHand))
//            {
//                Log.d("septica", "sunt in ....");
//                decideWinner(foldButton);
//            }

            //decide if human player can cut
//            else if (isHumanWhoStartedHand)
//            {
//                boolean cantCut = false;
//
//                //search for cards to cut with, in human cards
//                for (int i = 0; i < humanCards.size(); ++i)
//                {
//                    if ((humanCards.get(i).getValue() == centerCards.get(0).getValue()) || (humanCards.get(i).getValue() == 7))
//                    {
//                        cantCut = true;
//                        break;
//                    }
//                }
//
//                //if no cards to cut with, fold
//                if (!cantCut)
//                    decideWinner(foldButton);
//            }

//        }
//        else //not the first card and computer is serving
//        {
//
//            computerComplexMove();
//        }
//


    }

    public void computerComplexMove() {
        Log.d("septica", "complex move");

        blockCards = true;
        ArrayList<Card> cardsToCutWith = new ArrayList<Card>();

        //decide if computer can cut
        for (Card c : computerCards)
        {
            if (c.getValue() == 7 || c.getValue() == centerCards.get(0).getValue())
            {
                cardsToCutWith.add(c);
            }
        }

        if (cardsToCutWith.size() > 0)//computer has cards to cut with
        {
            int randomN = (int) (Math.random() * 10 % 2);

            if (randomN == 0)//computer doesn't want to cut(fold)
            {
                blockCards = false;
                decideWinner(foldButton);
            } else//computer wants to cut
            {
                int randomN2 = (int) (Math.random() * 10 % cardsToCutWith.size());
                for (int i = 0; i < computerCards.size(); ++i)
                {
                    if (cardsToCutWith.get(randomN2) == computerCards.get(i))
                    {
                        centerCards.add(computerCards.remove(i));
                        Log.d("septica", "sunt in taiere");
                    }
                }
                isHumanNext = true;
                computerMoveUI();
                blockCards = false;

            }
        } else//computer has no cards to cut with
        {
            blockCards = false;
            decideWinner(foldButton);
        }

    }

    public void computerSimpleMove()
    {
        if(computerCards.size() == 0 && deck.size() == 0)
        {
            return;
        }
        int randomChoice = (int) (Math.random() * 10 % computerCards.size());
        centerCards.add(computerCards.remove(randomChoice));
        Log.d("septica", "sunt in simple move");
        isHumanNext = true;

    }

    public void computerMoveUI() {

        turnBallComputer.setVisibility(View.INVISIBLE);
        turnBallHuman.setVisibility(View.VISIBLE);

        //enable fold button when human is serving, at second human move
        if ((isHumanWhoStartedHand && centerCards.size() == 2) && isHumanNext)
            foldButton.setEnabled(true);

        //computer setup
        for (int i = 0; i < computerCards.size(); ++i) {
            computerCardsImageView[i].setImageResource(R.drawable.az);
        }

        if (computerCards.size() < 4) {
            for (int i = computerCards.size(); i < 4; ++i) {
                computerCardsImageView[i].setVisibility(View.INVISIBLE);
            }
        }

        //center setup
        for (int i = 0; i < centerCards.size(); ++i) {
            centerCardsImageView[i].setVisibility(View.VISIBLE);
            centerCardsImageView[i].setImageResource(centerCards.get(i).getResourceId());
        }

    }

    public void decideWinner(View v)//also computer makes a simple move if he wins the hand
    {
        //if human is serving
        if (isHumanWhoStartedHand) {
            //if computer wins hand
            if (isWinner()) {
                computerWinsHand();

            } else//if human wins hand
            {
                humanWinsHand();
            }
        } else {
            //computer is serving
            //if human wins hand
            if (isWinner()) {
                humanWinsHand();

            } else//if computer wins hand
            {
                computerWinsHand();

            }
        }

        if (deck.size() == 0 && computerCards.size() == 0)
        {
            createAlertDialogWin();

        }
    }

    private void humanWinsHand() {

        humanPoints += calculatePoints();
        isHumanWhoStartedHand = true;
        isHumanNext = true;
        foldButton.setEnabled(false);
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                humanWinMessage.setText("You won this hand");
                blockCards = true;
                SystemClock.sleep(1000);
                setupHand();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        humanWinMessage.setText("");
                        turnBallHuman.setVisibility(View.VISIBLE);
                        turnBallComputer.setVisibility(View.INVISIBLE);
                        setupHandUI();
                        blockCards = false;
                    }
                });
            }
        });
        myThread.start();
    }

    private void computerWinsHand()
    {
        isHumanNext = false;
        computerPoints += calculatePoints();
        isHumanWhoStartedHand = false;
        foldButton.setEnabled(false);

        computerWinMessage.setText("Computer wins hand");
        Thread myThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                blockCards = true;
                SystemClock.sleep(1000);
                setupHand();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        turnBallComputer.setVisibility(View.VISIBLE);
                        turnBallHuman.setVisibility(View.INVISIBLE);
                        setupHandUI();
                        computerWinMessage.setText("");
                        computerMove();
                        blockCards = false;
                    }
                });
            }
        });
        myThread.start();
    }

    private boolean isWinner() {
        return (centerCards.get(0).getValue() == centerCards.get(centerCards.size() - 1).getValue()) ||
                (centerCards.get(centerCards.size() - 1).getValue() == 7);
    }

    public void createDecks() {
        int firstCardId = R.drawable.ac07;

        deck = new ArrayList<>(32);

        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                deck.add(new Card(suit, rank, firstCardId));
                firstCardId += 1;
            }
        }

        Collections.shuffle(deck);

        humanCards = new ArrayList<>(4);
        computerCards = new ArrayList<>(4);
        centerCards = new ArrayList<>(8);
    }

    public void initializeCards() {
        foldButton = findViewById(R.id.fold_button);

        computerCardsImageView = new ImageView[4];
        computerCardsImageView[0] = findViewById(R.id.computer_1c);
        computerCardsImageView[1] = findViewById(R.id.computer_2c);
        computerCardsImageView[2] = findViewById(R.id.computer_3c);
        computerCardsImageView[3] = findViewById(R.id.computer_4c);

        humanCardsImageView = new ImageView[4];
        humanCardsImageView[0] = findViewById(R.id.human_1c);
        humanCardsImageView[1] = findViewById(R.id.human_2c);
        humanCardsImageView[2] = findViewById(R.id.human_3c);
        humanCardsImageView[3] = findViewById(R.id.human_4c);

        centerCardsImageView = new ImageView[8];
        centerCardsImageView[0] = findViewById(R.id.first_card);
        centerCardsImageView[1] = findViewById(R.id.second_card);
        centerCardsImageView[2] = findViewById(R.id.third_card);
        centerCardsImageView[3] = findViewById(R.id.fourth_card);
        centerCardsImageView[4] = findViewById(R.id.fifth_card);
        centerCardsImageView[5] = findViewById(R.id.sixth_card);
        centerCardsImageView[6] = findViewById(R.id.seventh_card);
        centerCardsImageView[7] = findViewById(R.id.eighth_card);

        computerPointsView = findViewById(R.id.computer_points);
        humanPointsView = findViewById(R.id.human_points);

        computerWinMessage = findViewById(R.id.computer_win_message);
        humanWinMessage = findViewById(R.id.human_win_message);

        turnBallComputer = findViewById(R.id.turn_ball_computer);
        turnBallHuman = findViewById(R.id.turn_ball_human);

        computerSetPoints = findViewById(R.id.computer_set_points);
        humanSetPoints = findViewById(R.id.human_set_points);
    }

    public void setupHand() {
        if (deck.size() > 0) {
            int cardsToDistribute = 4 - humanCards.size();
            if (cardsToDistribute > deck.size() / 2)
                cardsToDistribute = deck.size() / 2;
            for (int i = 0; i < cardsToDistribute; ++i) {
                humanCards.add(deck.remove(0));
                computerCards.add(deck.remove(0));
            }
        }
        centerCards.clear();

    }

    public void setupHandUI() {
        //computer setup
        setupCards(computerCards, computerCardsImageView);

        //human setup
        setupCards(humanCards, humanCardsImageView);

        //center cards
        setupCenterCards();

        computerPointsView.setText(String.valueOf(computerPoints));
        humanPointsView.setText(String.valueOf(humanPoints));
    }

    private void setupCenterCards() {
        for (int i = 0; i < centerCardsImageView.length; ++i) {

            centerCardsImageView[i].setVisibility(View.GONE);
        }
    }

    private void setupCards(ArrayList<Card> listOfCards, ImageView[] cardsImageView) {
        if(listOfCards == this.computerCards) {
            for (int i = 0; i < listOfCards.size(); ++i) {
                cardsImageView[i].setImageResource(R.drawable.az);
                cardsImageView[i].setVisibility(View.VISIBLE);
            }
        }
        else {
            for (int i = 0; i < listOfCards.size(); ++i) {
                cardsImageView[i].setImageResource(listOfCards.get(i).getResourceId());
                cardsImageView[i].setVisibility(View.VISIBLE);
            }
        }
        if (listOfCards.size() < 4) {
            for (int i = listOfCards.size(); i < 4; ++i) {
                cardsImageView[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    public int calculatePoints() {
        int numberOfPoints = 0;

        //check points and return number of points
        for (Card c : centerCards) {
            if (c.getValue() == 10 || c.getValue() == 11) {
                numberOfPoints++;

            }
        }
        return numberOfPoints;
    }

    public void resetGame()
    {
        //reset points
        computerPointsView.setText(String.valueOf(0));
        computerPoints = 0;
        humanPointsView.setText(String.valueOf(0));
        humanPoints = 0;

        //setup
        createDecks();
        setupHand();
        setupHandUI();

        //decide first player
        int randomN = (int) (Math.random() * 10 % 2);
        if (randomN == 0) //computer first serve
        {
            turnBallHuman.setVisibility(View.INVISIBLE);
            isHumanWhoStartedHand = false;
            isHumanNext = false;
            foldButton.setEnabled(false);
            computerMove();
        }
        else//human first serve
        {
            turnBallComputer.setVisibility(View.INVISIBLE);
            isHumanWhoStartedHand = true;
            isHumanNext = true;
            foldButton.setEnabled(false);
        }
    }

    public void createAlertDialogWin()
    {
        String whoWins;
        if (computerPoints > humanPoints) {
            computerGeneralScore += 1;
            whoWins = "Computer wins the game";
        }
        else if(computerPoints < humanPoints)
        {
            humanGeneralScore += 1;
            whoWins = "Human wins the game";
        }
        else
            whoWins = "It's a tie";

        computerSetPoints.setText(String.valueOf(computerGeneralScore));
        humanSetPoints.setText(String.valueOf(humanGeneralScore));    

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("End of game");
        myAlertDialog.setCancelable(false);
        myAlertDialog.setMessage(whoWins + "\nGeneral Score\nHuman " + humanGeneralScore + " : " + computerGeneralScore + " Computer");

        myAlertDialog.setPositiveButton("EXIT", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                finish();
            }
        });

        myAlertDialog.setNegativeButton("CONTINUE", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                resetGame();
            }
        });
        myAlertDialog.show();
    }

}