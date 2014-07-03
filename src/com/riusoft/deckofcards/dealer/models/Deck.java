package com.riusoft.deckofcards.dealer.models;

import com.google.gson.Gson;
import com.riusoft.deckofcards.dealer.DealerServer;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Author: Amanda Riu
 * Date: 6/30/2014
 * Summary: Class representing a deck of cards.
 */
public class Deck {
    private ArrayList<Card> cards = new ArrayList<Card>();

    /**
     * Transient to prevent auto-serialization
     */
    private transient int[] values = {1,2,3,4,5,6,7,8,9,10,11,12,13};
    private transient String[] suits = {"Club","Spade","Diamond","Heart"};

    /**
     * Constructs a deck of cards. Loops through the suits and for
     * each suit, loops through all the possible values, and generates
     * a new Card object for each. Then it shuffles the deck.
     */
    public Deck() {
        // Create the initial deck of unsorted cards
        for (int i = 0; i < suits.length; i++) {
            for (int j = 0; j < values.length; j++) {
                this.cards.add(new Card(suits[i], values[j]));
            }
        }

        // Do the initial shuffle of the cards.
        shuffleCards();
    }

    /**
     * Return the current working deck of cards.
     * @return
     */
    public ArrayList<Card> getCards() {
        return this.cards;
    }

    /**
     * Shuffles the current deck of cards and returns the deck.
     * @return The newly shuffled deck of cards.
     */
    public ArrayList<Card> getShuffledCards() {
        shuffleCards();
        return this.cards;
    }

    /**
     * Will shuffle the current deck of cards.
     */
    public void shuffleCards() {
        Collections.shuffle(this.cards);
    }

    /**
     * Serialize this deck into a JSON Object
     */
    public String serialize() {
        Gson gson = new Gson();
        String flat = gson.toJson(this);
        DealerServer.log("Serialized deck: " + flat);
        return flat;
    }
}
