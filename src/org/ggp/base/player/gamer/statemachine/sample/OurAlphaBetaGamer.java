package org.ggp.base.player.gamer.statemachine.sample;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * SampleLegalGamer is a minimal gamer which always plays the first
 * legal move it identifies, regardless of the state of the game.
 *
 * For your first players, you should extend the class SampleGamer
 * The only function that you are required to override is :
 * public Move stateMachineSelectMove(long timeout)
 *
 */
public final class OurAlphaBetaGamer extends SampleGamer {
	/**
	 * This function is called at the start of each round
	 * You are required to return the Move your player will play
	 * before the timeout.	 */
	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException	{
		// We get the current start time
		long start = System.currentTimeMillis();

		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		Move bestmove = moves.get(0);
		int score = 0;
		for (int i = 0; i < moves.size(); i++) {
			Move move = moves.get(i);
			int result = minscore(getRole(), move, getCurrentState(), 0, 100);
			if (result > score)		{
				score = result;
				bestmove = move;
			}
		}

		// We get the end time ... It is mandatory that stop<timeout
		long stop = System.currentTimeMillis();

		// leave this
		notifyObservers(new GamerSelectedMoveEvent(moves, bestmove, stop - start));
		return bestmove;
	}

	private int minscore(Role my_role, Move move, MachineState state, int alpha, int beta) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		Role opponent = getStateMachine().getRoles().get(1);
		List<Move> temp = new ArrayList<Move>();
		temp.add(move);
		List<Move> opps_moves = getStateMachine().getLegalMoves(state, opponent);
//		int score = 100;
		for (int i = 0; i < opps_moves.size(); i++) {
			List<Move> moves = new ArrayList<Move>();
			moves.add(move);
			moves.add(opps_moves.get(i));

			MachineState newstate = getStateMachine().getNextState(state, moves);

			int result = maxscore(my_role, newstate, alpha, beta);
			beta = min(beta, result);
			if (beta <= alpha)	return alpha;
		}
		return beta;
	}

	private int maxscore(Role my_role, MachineState state, int alpha, int beta) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		if (getStateMachine().isTerminal(state))		return getStateMachine().getGoal(state, my_role);
		List<Move> my_moves = getStateMachine().getLegalMoves(state, my_role);
	//	int maxscore = 0;
		for (int i = 0; i < my_moves.size(); i++) {
			int result = minscore(my_role, my_moves.get(i), state, alpha, beta);
			alpha = max(alpha, result);
			if (alpha >= beta) 	return beta;
		}
		return alpha;
	}


	private int min(int x, int y) {
		if (x < y) return x;
		return y;
	}
	private int max(int x, int y) {
		if (x > y) return x;
		return y;
	}
}


// WANT:
// - get_opponents_role()
// - get next state from current move
// - is a state my_move + opp_move  >>  do we have to make it move from state to state?
// - ask about A-B

