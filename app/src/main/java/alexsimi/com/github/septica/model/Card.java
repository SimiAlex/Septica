package alexsimi.com.github.septica.model;

public class Card
{
    // A card
    private Suit suit;
    private Rank rank;

    private int resourceId;

    public Card(Suit suit, Rank rank, int resourceId)
    {   // constructor
        this.suit = suit;
        this.rank = rank;
        this.resourceId = resourceId;
    }

    public Suit getSuit() { return suit; }
    public Rank getRank() { return rank; }
    public int getValue() { return rank.ordinal() + 7; }  // 7 to 14
    public String toString() { return suit + "-" + rank; }
    public int getResourceId() { return resourceId; }
}
