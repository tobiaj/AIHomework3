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

        if (agentNumber != boardSize) {
            createSubscription();

        }
        else{
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
                        System.out.println("agent " + getLocalName() + " hoppar in i move");
                        waitForMove();
                    }

                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
    }

    private void firstMove() {
        System.out.println("kommer vi hit???");

        board[agentNumber][xPosition] = -1;

        backUpboard = board;

        fillBoard(agentNumber, xPosition);

        System.out.println("skapar message");

        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setOntology("move");

        try {
            message.setContentObject(board);
        } catch (IOException e) {
            e.printStackTrace();
        }

        message.addReceiver(nextAgentAID);
        send(message);

        System.out.println("Skickat message");

        waitForMove();
    }

    private boolean areWeUnderAttack() {

        for (int i = 0; i < board.length; i++) {
            if (board[agentNumber][i] < 1) {
                board[agentNumber][i] = -1;
                prevYPosition = i;
                if (agentNumber == boardSize){

                }
                return false;
            }
        }
        return true;
    }

    private void fillBoard(int yPosition, int xPosition) {

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (i == yPosition && board[i][j] > -1)
                    board[i][j]++;
                else if (j == xPosition && board[i][j] > -1)
                    board[i][j]++;
            }
        }

        int startX=0, startY=0;

        for (int i = yPosition, j = xPosition; i >= 0 && j >= 0; i--, j--) {
            if (i ==0 || j==0) {
                startX = j;
                startY = i;
                break;
            }
        }

        for (int i = startX, j = startY; i < board.length && j < board.length; i++,j++){
            if (board[i][j] != -1)
                board[i][j]++;
        }


        for (int i = yPosition, j = xPosition; i < board.length && j >= 0; i++, j--) {
            //System.out.println("TESTAR " + i + " " + j);
            if (i ==0 || j==0) {
                startX = j;
                startY = i;
                break;
            }
        }


        System.out.println("startX: " + startX + "\n startY: " + startY);

        for (int i = startX, j = startY; i >= 0 && j < board.length; i--,j++){
            System.out.println(i + " and " + j);
            if (board[i][j] != -1)
                board[i][j]++;
        }

        printBoard();

    }

    private void waitForMove() {
        System.out.println("agent: " + getLocalName() + " kommer hit");
        MessageTemplate messageTemplate = MessageTemplate.or(MessageTemplate.MatchOntology("move"), MessageTemplate.MatchOntology("callback"));

        ReceiveMove receiveMove = new ReceiveMove(this, messageTemplate, Long.MAX_VALUE, null, null);

        addBehaviour(receiveMove);
    }

    public class ReceiveMove extends MsgReceiver{


        public ReceiveMove(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
            super(a, mt, deadline, s, msgKey);
        }

        @Override
        protected void handleMessage(ACLMessage msg) {

            System.out.println("Queen: " + agentNumber + " received a message from queen " + msg.getSender());
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);

            if (msg.getOntology().equals("move")) {

                previousAgentAID = msg.getSender();

                try {
                    board = (int[][]) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }

                if (!areWeUnderAttack()) {
                    fillBoard(agentNumber, xPosition);
                    if (agentNumber == boardSize){
                        doDelete();//End program?
                    }
                    message.setOntology("move");
                    message.addReceiver(nextAgentAID);
                    send(message);
                } else {
                    board = backUpboard; //unfill board
                    message.setOntology("callback");
                    message.addReceiver(previousAgentAID);
                    send(message);
                }

            }
            else if(msg.getOntology().equals("callback")){

            }
        }

        @Override
        public int onEnd() {
            myAgent.addBehaviour(this);
            return super.onEnd();
        }
    }

}
