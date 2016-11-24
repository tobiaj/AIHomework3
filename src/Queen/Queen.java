package Queen;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionInitiator;
import jade.proto.states.MsgReceiver;

import java.io.IOException;

/**
 * Created by tobiaj on 2016-11-23.
 */
public class Queen extends Agent {

    private int agentNumber;
    public AID nextAgentAID;
    public AID previousAgentAID;
    public int xPosition = 0;
    public int prevYPosition;
    public int [][] board;
    public int [][] backUpboard;
    public int boardSize;


    public void setup(){

        boardSize = 4;
        Object [] arguemnts = getArguments();

        if (arguemnts.length > 1 ){
            agentNumber = Integer.parseInt((String) arguemnts[0]);
            boardSize = Integer.parseInt((String) arguemnts[1]);
        }

        else if (arguemnts.length > 0 ){
            agentNumber = Integer.parseInt((String) arguemnts[0]);
        }

        System.out.println("Queen number: " + agentNumber + " has started");
        board = new int[boardSize][boardSize];

        if (agentNumber == 0) {
            addBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    initiateBoard();
                }
            });
            printBoard();
        }

        String service = "Queen" + agentNumber;
        registerService(this, service);

        if (agentNumber < board.length) {
            createSubscription();

        }
        else{
            System.out.println("");
            waitForMove();
        }
    }

    private void initiateBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = 0;
            }
        }
    }

    private void printBoard() {
        for (int i = 0; i < board.length; i++) {
            System.out.println("\n-----------");
            for (int j = 0; j < board.length; j++) {
                System.out.print(board[i][j] + " ");
            }
        }
    }

    public void printQueens() {
        int n = board.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] == -1) System.out.print("Q ");
                else           System.out.print("* ");
            }
            System.out.println();
        }
        System.out.println();
    }


    public void registerService(Agent agent, String service) {

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());

        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(service);
        serviceDescription.setName(agent.getName());
        dfd.addServices(serviceDescription);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

    }

    private void createSubscription() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("Queen" + (agentNumber + 1));
        SearchConstraints search = new SearchConstraints();

        template.addServices(serviceDescription);

        Subscribe subscribe = new Subscribe(this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, search));

        addBehaviour(subscribe);

    }

    public class Subscribe extends SubscriptionInitiator {

        public Subscribe(Agent agent, ACLMessage message) {
            super(agent, message);
        }

        protected void handleInform(ACLMessage inform) {
            try {
                DFAgentDescription[] result = DFService.decodeNotification(inform.getContent());
                if (result.length > 0) {
                    System.out.println("Queen number: " + agentNumber + " received a subscription message from " + result[0].getName().getName());
                    nextAgentAID = result[0].getName();

                    if (agentNumber == 0) {
                        firstMove();
                    } else {
                        waitForMove();
                    }

                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
    }

    private void firstMove() {

        board[agentNumber][xPosition] = -1;

        backUpboard = board;

        fillBoard(agentNumber, xPosition);

        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setOntology("move"+(agentNumber+1));

        try {
            message.setContentObject(board);
        } catch (IOException e) {
            e.printStackTrace();
        }

        message.addReceiver(nextAgentAID);
        send(message);

        waitForMove();
    }

    private boolean areWeUnderAttack(int yPosition) {

        for (int i = yPosition; i < board.length; i++) {
            if (board[agentNumber][i] < 1) {
                prevYPosition = i;
                return false;
            }
        }
        return true;
    }

    private void fillBoard(int xPosition, int yPosition) {

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (i == xPosition && board[i][j] > -1)
                    board[i][j]++;
                else if (j == yPosition && board[i][j] > -1)
                    board[i][j]++;
            }
        }

        System.out.println("EFTER lodrät vågrätt LOOP");
        printBoard();

        int startX=0, startY=0;

        for (int i = xPosition, j = yPosition; i >= 0 && j >= 0; i--, j--) {
            if (i ==0 || j==0) {
                startX = i;
                startY = j;
                break;
            }
        }

        System.out.println("Start position för första diagonal: " + startX + " " + startY);

        for (int i = startX, j = startY; i < board.length && j < board.length; i++,j++){
            if (board[i][j] != -1)
                board[i][j]++;
        }

        System.out.println("EFTER FÖRSTA diagonal");
        printBoard();


        for (int i = xPosition, j = yPosition; j < board.length && i >= 0; i--, j++) {
            //System.out.println("TESTAR " + i + " " + j);
            if (i == 0 || j == board.length - 1) {
                startX = i;
                startY = j;
                break;
            }
        }

        System.out.println("Start position för andra");

        System.out.println("startX: " + startX + "\n startY: " + startY);

        for (int i = startX, j = startY; j >= 0 && i < board.length; i++,j--){
            System.out.println(i + " and " + j);
            if (board[i][j] != -1)
                board[i][j]++;
        }
        System.out.println("EFTER Andra diagonal");

        printBoard();

        System.out.println("\n");

    }

    private void unFillBoard(int yPosition, int xPosition) {

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (i == xPosition && board[i][j] > -1)
                    board[i][j]--;
                else if (j == yPosition && board[i][j] > -1)
                    board[i][j]--;
            }
        }

        System.out.println("EFTER lodrät vågrätt LOOP UNFILL");
        printBoard();

        int startX=0, startY=0;

        for (int i = xPosition, j = yPosition; i >= 0 && j >= 0; i--, j--) {
            if (i ==0 || j==0) {
                startX = i;
                startY = j;
                break;
            }
        }

        System.out.println("Start position för första diagonal:  UNFILL" + startX + " " + startY);

        for (int i = startX, j = startY; i < board.length && j < board.length; i++,j++){
            if (board[i][j] != -1)
                board[i][j]--;
        }

        System.out.println("EFTER FÖRSTA diagonal  UNFILL");
        printBoard();


        for (int i = xPosition, j = yPosition; j < board.length && i >= 0; i--, j++) {
            //System.out.println("TESTAR " + i + " " + j);
            if (i == 0 || j == board.length - 1) {
                startX = i;
                startY = j;
                break;
            }
        }

        System.out.println("Start position för andra  UNFILL");

        System.out.println("startX: " + startX + "\n startY: " + startY);

        for (int i = startX, j = startY; j >= 0 && i < board.length; i++,j--){
            System.out.println(i + " and " + j);
            if (board[i][j] != -1)
                board[i][j]--;
        }
        System.out.println("EFTER Andra diagonal  UNFILL");

        board[agentNumber][prevYPosition] = 0;
        printBoard();

        System.out.println("\n");

    }

    private void waitForMove() {
        System.out.println("Queen: " + agentNumber + " agent: " + getLocalName() + " kommer hit");
        MessageTemplate messageTemplate = MessageTemplate.or(MessageTemplate.MatchOntology("move" + agentNumber), MessageTemplate.MatchOntology("callback"));

        ReceiveMove receiveMove = new ReceiveMove(this, messageTemplate, Long.MAX_VALUE, null, null);

        addBehaviour(receiveMove);
    }

    public class ReceiveMove extends MsgReceiver{


        public ReceiveMove(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
            super(a, mt, deadline, s, msgKey);
        }

        @Override
        protected void handleMessage(ACLMessage msg) {

            System.out.println("Queen: " + agentNumber + " received a message from queen " + msg.getSender() + " with ontology " + msg.getOntology());
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);

            if (msg.getOntology().equals("move"+agentNumber)) {

                previousAgentAID = msg.getSender();
                System.out.println("AID OF MESSAGE RECEIVED: " + msg.getSender());
                msg.clearAllReceiver();
                message.clearAllReceiver();

                try {
                    board = (int[][]) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }

                if (!areWeUnderAttack(prevYPosition)) {
                    placeQueen(prevYPosition);
                    fillBoard(agentNumber, prevYPosition);
                    if (agentNumber == boardSize){
                        System.out.println("KLARA!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    }
                    message.setOntology("move"+(agentNumber+1));
                    message.addReceiver(nextAgentAID);
                    try {
                        message.setContentObject(board);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    send(message);
                } else {
                    message.setOntology("callback");
                    message.addReceiver(previousAgentAID);
                    send(message);
                }

            }
            else if(msg.getOntology().equals("callback")) {
                unFillBoard(agentNumber, prevYPosition);
                if (prevYPosition + 1 > board.length){
                    prevYPosition = 0;

                    message.setOntology("callback");
                    message.addReceiver(previousAgentAID);
                    send(message);

                }

                if (!areWeUnderAttack(prevYPosition + 1)) {
                    System.out.println("PREVIOUS INNAN FILLBOARD IGEN EFTER EN CALLBACK: " + prevYPosition);
                    placeQueen(prevYPosition);
                    fillBoard(agentNumber, prevYPosition);
                    System.out.println("PREVIOUS EFTER FILLBOARD IGEN EFTER EN CALLBACK: " + prevYPosition);

                    message.setOntology("move"+(agentNumber+1));
                    message.addReceiver(nextAgentAID);
                    try {
                        message.setContentObject(board);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    send(message);
                } else {
                    message.setOntology("callback");
                    message.addReceiver(previousAgentAID);
                    send(message);
                }
            }
        }

        @Override
        public int onEnd() {
            myAgent.addBehaviour(this);
            return super.onEnd();
        }
    }

    private void placeQueen(int yPos) {
        board[agentNumber][yPos] = -1;
    }

}


