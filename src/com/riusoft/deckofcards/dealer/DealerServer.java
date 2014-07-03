package com.riusoft.deckofcards.dealer;

import com.riusoft.deckofcards.dealer.models.Deck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

/**
 * Author: Amanda Riu
 * Date: 6/30/2014
 *
 * A multi-threaded Card Shuffle Game. This is the 'Dealer' application that works with
 * PlayerClient Applications.
 *
 * Dealer will shuffle a deck of cards and deliver it to any connected 'player' processes. When
 * asked to reshuffle the cards, the dealer will shuffle the cards and deliver the newly
 * shuffled cards to all players currently connected to the game.
 */
public class DealerServer {

    /**
     * The port this server listens on
     */
    private static final int PORT = 60451;

    /**
     * The static key we expect from clients as a request to
     * shuffle the deck
     */
    private static final String KEY_SHUFFLE = "SHUFFLE";

    /**
     * The static key we expect from clients as a request
     * to disconnect
     */
    private static final String KEY_DISCONNECT = "DISCONNECT";

    /**
     * A HashSet of all the printer writers for attached Players.
     * We keep this so we can easily broadcast changes across all
     * players.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    /**
     * Our deck of cards. Since we will only be using a single deck of
     * cards, we will simply make this final.
     */
    private static final Deck deck = new Deck();


    /**
     * Application main method. Listens on the designated port, and
     * spawns new handler threads for new connections.
     */
    public static void main(String[] args) {
        log("Dealer Server is now running on port [" + PORT + "]");
        ServerSocket listener = null;

        // Instantiate our socket and listen on the specified port.
        try {
            listener = new ServerSocket(PORT);
        } catch (IOException io) {
            log("IOException while instantiating a listener to socket on port ["
                    + PORT + "]");
            io.printStackTrace();
        }

        // Continuous loop to check the socket for new connections. If
        // we get a new connection, spawn a handler thread for processing.
        try {
            while (true) {
                try {
                    new PlayerHandler(listener.accept()).start();
                } catch (IOException io) {
                    log("IOException: " + io.getMessage());
                    io.printStackTrace();
                }
            }
        } finally {
            try {
                listener.close();
            } catch (IOException io) {
                log("IOException: Unable to close Server Socket");
                io.printStackTrace();
            }
        }
    } // END main()


    /**
     * Basic logger shortcut for printing messages to terminal.
     * In a production system this would be a log instead.
     * @param msg
     */
    public static void log(String msg) {
        System.out.println(msg);
    }


    /**
     * A request to reshuffle the deck has been received. Reshuffle the
     * deck, get the ArrayList<> of shuffled cards, and then turn that list
     * into a JSON document. Then notify all listening players by sending
     * them this JSON document so they can redraw their screen.
     */
    public static void reshuffleDeck() {
        // shuffle the deck
        log("Shuffling the deck!");
        deck.shuffleCards();

        // Serialize the deck
        String deckString = deck.serialize();
        log("Serialized deck to : " + deckString);

        // Notify the listening players
        for (PrintWriter writer : writers) {
            try {
                log("Sending serialized deck to writer");
                writer.println(deckString);
            } catch (Exception e) {
                log("Error writing to one of the players: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    /**
     * A handler thread class. Handlers are spawned from the listening
     * loop and are responsible for dealing witha single player
     * connection.
     */
    private static class PlayerHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructor
         */
        public PlayerHandler(Socket socket) {
            log("New connection with player client: " + socket.getRemoteSocketAddress());
            this.socket = socket;
        }

        /**
         * Services the Player Thread by listening for activity
         * in an infinite loop.
         */
        public void run() {
            try {
                // Create the character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(),true);

                // Track whether or not we should break out of loop
                boolean done = false;

                // Add the socket's print writer to our HashSet of
                // writers so we can broadcast updated decks.
                writers.add(out);

                // Send the newly connected player our current
                // deck of cards
                out.println(deck.serialize());

                // Accept messages from this client. If we get a
                // SHUFFLE command, we will shuffle the deck of
                // cards and then send them out to all the
                // registered players.
                while(!done) {
                    try {
                        String input = in.readLine();
                        log("Message received from Player: [" + input + "]");
                        if (input == null) {
                            break;
                        } else if (input != null && KEY_SHUFFLE.equals(input.toUpperCase())) {
                            log("We've received a SHUFFLE REQUEST!");
                            reshuffleDeck();
                        } else if (input != null && KEY_DISCONNECT.equals(input.toUpperCase())) {
                            log("Received a disconnect request from client.");
                            done = true;
                        }
                    } catch (IOException io) {
                        log("IOException while trying to read input from player "
                            + io.getMessage());
                        io.printStackTrace();
                    }
                }
            } catch (IOException io) {
                log("IOException: " + io.getMessage());
                io.printStackTrace();
            } finally {
                // The client is going down. Remove from our HashSet, close our
                // streams and attempt to close the socket.
                if (out != null) {
                    writers.remove(out);
                }

                try {
                    in.close();
                    out.close();
                } catch (IOException io1) { /* do nothing */ }

                try {
                    socket.close();
                } catch (IOException io2) { /* do nothing */ }
            }
        } // END run()
    }
}
