package com.riusoft.deckofcards.dealer.models;

/**
 * Author: Amanda Riu
 * Date: 6/30/2014
 * Summary: This class represents a single card in a deck of playing cards.
 */
public class Card {

    private String suit;
    private int value;

    /**
     * Constructs a new card using the values provided.
     *
     * @param s The suit of the card.
     * @param v The value of the card (1=Ace, 11=Jack, 12=Queen, 13=King)
     */
    public Card(String s, int v){
        this.suit = s;
        this.value = v;
    }

    public String getSuit(){
        return suit;
    }
    public void setSuit(String suit){
        this.suit = suit;
    }

    public int getValue(){
        return value;
    }
    public void setValue(int value){
        this.value = value;
    }

    public String toString(){
        return "\n"+value + " of "+ suit;
    }
}
