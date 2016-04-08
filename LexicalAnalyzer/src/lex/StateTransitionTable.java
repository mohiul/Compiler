package lex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class StateTransitionTable {
	private Map<Pair<Integer, Character>, Integer> stateTransitionTable;
	private Map<Integer, String> finalStates;
	private Set<String> reservedWords;
	char symbols[] = {'l', 'd', '_', 'z', '0', '.', '/', '*', '\n', '[', ']', '{', '}', '(', ')', '<', '=', '>', ';', ',', '+', '-', ' '};
	
	public StateTransitionTable() {
		stateTransitionTable = new HashMap<>();
		finalStates = new HashMap<Integer, String>();
		reservedWords = new TreeSet<>();
		
		stateTransitionTable.put(new Pair<Integer, Character>(0, 'l'), 1);
		stateTransitionTable.put(new Pair<Integer, Character>(0, 'd'), 3);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '_'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(0, 'z'), 2);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '0'), 3);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '.'), 33);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '/'), 13);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '*'), 36);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '\n'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '['), 19);
		stateTransitionTable.put(new Pair<Integer, Character>(0, ']'), 18);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '{'), 21);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '}'), 20);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '('), 22);
		stateTransitionTable.put(new Pair<Integer, Character>(0, ')'), 23);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '<'), 24);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '='), 29);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '>'), 27);
		stateTransitionTable.put(new Pair<Integer, Character>(0, ';'), 31);
		stateTransitionTable.put(new Pair<Integer, Character>(0, ','), 32);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '+'), 34);
		stateTransitionTable.put(new Pair<Integer, Character>(0, '-'), 35);
		stateTransitionTable.put(new Pair<Integer, Character>(0, ' '), 0);
		
		stateTransitionTable.put(new Pair<Integer, Character>(1, 'l'), 4);
		stateTransitionTable.put(new Pair<Integer, Character>(1, 'd'), 5);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '_'), 6);
		stateTransitionTable.put(new Pair<Integer, Character>(1, 'z'), 5);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '0'), 5);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '.'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '/'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '*'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '\n'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '['), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, ']'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '{'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '}'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '('), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, ')'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '<'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '='), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '>'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, ';'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, ','), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '+'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, '-'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(1, ' '), 0);
		finalStates.put(1, Constants.ID);

		stateTransitionTable.put(new Pair<Integer, Character>(2, 'l'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(2, 'd'), 8);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '_'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(2, 'z'), 7);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '0'), 8);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '.'), 9);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '/'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '*'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '\n'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '['), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, ']'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '{'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '}'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '('), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, ')'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '<'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '='), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '>'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, ';'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, ','), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '+'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, '-'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(2, ' '), 0);
		finalStates.put(2, Constants.INTEGERNUM);

		stateTransitionTable.put(new Pair<Integer, Character>(3, 'l'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(3, 'd'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '_'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(3, 'z'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '0'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '.'), 9);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '/'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '*'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '\n'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '['), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, ']'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '{'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '}'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '('), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, ')'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '<'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '='), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '>'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, ';'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, ','), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '+'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, '-'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(3, ' '), 0);
		finalStates.put(3, Constants.INTEGERNUM);

		stateTransitionTable.put(new Pair<Integer, Character>(4, 'l'), 4);
		stateTransitionTable.put(new Pair<Integer, Character>(4, 'd'), 5);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '_'), 6);
		stateTransitionTable.put(new Pair<Integer, Character>(4, 'z'), 5);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '0'), 5);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '.'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '/'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '*'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '\n'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '['), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, ']'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '{'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '}'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '('), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, ')'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '<'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '='), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '>'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, ';'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, ','), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '+'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, '-'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(4, ' '), 0);
		finalStates.put(4, Constants.ID);

		stateTransitionTable.put(new Pair<Integer, Character>(5, 'l'), 4);
		stateTransitionTable.put(new Pair<Integer, Character>(5, 'd'), 5);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '_'), 6);
		stateTransitionTable.put(new Pair<Integer, Character>(5, 'z'), 5);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '0'), 5);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '.'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '/'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '*'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '\n'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '['), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, ']'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '{'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '}'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '('), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, ')'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '<'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '='), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '>'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, ';'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, ','), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '+'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, '-'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(5, ' '), 0);
		finalStates.put(5, Constants.ID);
		
		stateTransitionTable.put(new Pair<Integer, Character>(6, 'l'), 4);
		stateTransitionTable.put(new Pair<Integer, Character>(6, 'd'), 5);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '_'), 6);
		stateTransitionTable.put(new Pair<Integer, Character>(6, 'z'), 5);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '0'), 5);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '.'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '/'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '*'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '\n'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '['), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, ']'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '{'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '}'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '('), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, ')'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '<'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '='), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '>'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, ';'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, ','), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '+'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, '-'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(6, ' '), 0);
		finalStates.put(6, Constants.ID);
		
		stateTransitionTable.put(new Pair<Integer, Character>(7, 'l'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(7, 'd'), 8);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '_'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(7, 'z'), 7);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '0'), 8);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '.'), 9);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '/'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '*'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '\n'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '['), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, ']'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '{'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '}'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '('), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, ')'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '<'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '='), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '>'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, ';'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, ','), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '+'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, '-'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(7, ' '), 0);
		finalStates.put(7, Constants.INTEGERNUM);
		
		stateTransitionTable.put(new Pair<Integer, Character>(8, 'l'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(8, 'd'), 8);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '_'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(8, 'z'), 7);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '0'), 8);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '.'), 9);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '/'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '*'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '\n'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '['), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, ']'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '{'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '}'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '('), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, ')'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '<'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '='), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '>'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, ';'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, ','), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '+'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, '-'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(8, ' '), 0);
		finalStates.put(8, Constants.INTEGERNUM);
		
		stateTransitionTable.put(new Pair<Integer, Character>(9, 'l'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, 'd'), 11);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '_'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, 'z'), 10);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '0'), 11);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '.'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '/'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '*'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '\n'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '['), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, ']'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '{'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '}'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '('), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, ')'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '<'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '='), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '>'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, ';'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, ','), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '+'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, '-'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(9, ' '), Constants.ERR_STATE);

		stateTransitionTable.put(new Pair<Integer, Character>(10, 'l'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(10, 'd'), 12);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '_'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(10, 'z'), 10);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '0'), 12);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '.'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '/'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '*'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '\n'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '['), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, ']'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '{'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '}'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '('), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, ')'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '<'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '='), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '>'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, ';'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, ','), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '+'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, '-'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(10, ' '), 0);
		finalStates.put(10, Constants.FLOATNUM);
		
		stateTransitionTable.put(new Pair<Integer, Character>(11, 'l'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(11, 'd'), 12);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '_'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(11, 'z'), 10);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '0'), 12);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '.'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '/'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '*'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '\n'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '['), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, ']'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '{'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '}'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '('), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, ')'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '<'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '='), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '>'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, ';'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, ','), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '+'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, '-'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(11, ' '), 0);
		finalStates.put(11, Constants.FLOATNUM);
		
		stateTransitionTable.put(new Pair<Integer, Character>(12, 'l'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, 'd'), 12);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '_'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, 'z'), 10);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '0'), 12);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '.'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '/'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '*'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '\n'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '['), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, ']'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '{'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '}'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '('), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, ')'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '<'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '='), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '>'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, ';'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, ','), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '+'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, '-'), Constants.ERR_STATE);
		stateTransitionTable.put(new Pair<Integer, Character>(12, ' '), Constants.ERR_STATE);
		
		stateTransitionTable.put(new Pair<Integer, Character>(13, 'l'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, 'd'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '_'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, 'z'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '0'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '.'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '/'), 14);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '*'), 15);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '\n'),0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '['), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, ']'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '{'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '}'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '('), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, ')'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '<'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '='), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '>'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, ';'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, ','), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '+'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, '-'), 0);
		stateTransitionTable.put(new Pair<Integer, Character>(13, ' '), 0);
		finalStates.put(13, Constants.DIV);

		for(char ch:symbols){
			if(ch == '\n' ){
				stateTransitionTable.put(new Pair<Integer, Character>(14, ch), 17);
			} else {
				stateTransitionTable.put(new Pair<Integer, Character>(14, ch), 14);
			}
		}

		for(char ch:symbols){
			if(ch == '*' ){
				stateTransitionTable.put(new Pair<Integer, Character>(15, ch), 16);
			} else {
				stateTransitionTable.put(new Pair<Integer, Character>(15, ch), 15);
			}
		}
		
		for(char ch:symbols){
			if(ch == '/' ){
				stateTransitionTable.put(new Pair<Integer, Character>(16, ch), 17);			
			} else {
				stateTransitionTable.put(new Pair<Integer, Character>(16, ch), 15);			
			}
		}
		
		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(17, ch), 0);			
		}
		finalStates.put(17, Constants.COMMENT);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(18, ch), 0);			
		}
		finalStates.put(18, Constants.CLOSESQBRACKET);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(19, ch), 0);			
		}
		finalStates.put(19, Constants.OPENSQBRACKET);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(20, ch), 0);			
		}
		finalStates.put(20, Constants.CLOSECRLBRACKET);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(21, ch), 0);			
		}
		finalStates.put(21, Constants.OPENCRLBRACKET);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(22, ch), 0);			
		}
		finalStates.put(22, Constants.OPENPAR);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(23, ch), 0);			
		}
		finalStates.put(23, Constants.CLOSEPAR);

		for(char ch:symbols){
			if(ch == '=' ){
				stateTransitionTable.put(new Pair<Integer, Character>(24, ch), 25);			
			} else if (ch == '>' ){
				stateTransitionTable.put(new Pair<Integer, Character>(24, ch), 26);			
			} else {
				stateTransitionTable.put(new Pair<Integer, Character>(24, ch), 0);			
			}
		}
		finalStates.put(24, Constants.LT);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(25, ch), 0);			
		}
		finalStates.put(25, Constants.LESSEQ);
		
		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(26, ch), 0);			
		}
		finalStates.put(26, Constants.NOTEQ);

		for(char ch:symbols){
			if(ch == '=' ){
				stateTransitionTable.put(new Pair<Integer, Character>(27, ch), 28);			
			} else {
				stateTransitionTable.put(new Pair<Integer, Character>(27, ch), 0);			
			}
		}
		finalStates.put(27, Constants.GT);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(28, ch), 0);			
		}
		finalStates.put(28, Constants.GREATEQ);

		for(char ch:symbols){
			if(ch == '=' ){
				stateTransitionTable.put(new Pair<Integer, Character>(29, ch), 30);			
			} else {
				stateTransitionTable.put(new Pair<Integer, Character>(29, ch), 0);			
			}
		}
		finalStates.put(29, Constants.EQ);
		
		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(30, ch), 0);			
		}
		finalStates.put(30, Constants.EQCOMP);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(31, ch), 0);			
		}
		finalStates.put(31, Constants.SEMICOLON);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(32, ch), 0);			
		}
		finalStates.put(32, Constants.COMMA);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(33, ch), 0);			
		}
		finalStates.put(33, Constants.POINT);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(34, ch), 0);			
		}
		finalStates.put(34, Constants.PLUS);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(35, ch), 0);			
		}
		finalStates.put(35, Constants.MINUS);

		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(36, ch), 0);			
		}
		finalStates.put(36, Constants.MULTIPLY);
		
		for(char ch:symbols){
			stateTransitionTable.put(new Pair<Integer, Character>(37, ch), 0);			
		}
		finalStates.put(37, Constants.ERR);
		
		reservedWords.add(Constants.RESERVED_WORD_AND);
		reservedWords.add(Constants.RESERVED_WORD_NOT);
		reservedWords.add(Constants.RESERVED_WORD_OR);
		reservedWords.add(Constants.RESERVED_WORD_IF);
		reservedWords.add(Constants.RESERVED_WORD_THEN);
		reservedWords.add(Constants.RESERVED_WORD_ELSE);
		reservedWords.add(Constants.RESERVED_WORD_FOR);
		reservedWords.add(Constants.RESERVED_WORD_CLASS);
		reservedWords.add(Constants.RESERVED_WORD_INT);
		reservedWords.add(Constants.RESERVED_WORD_FLOAT);
		reservedWords.add(Constants.RESERVED_WORD_GET);
		reservedWords.add(Constants.RESERVED_WORD_PUT);
		reservedWords.add(Constants.RESERVED_WORD_RETURN);
		reservedWords.add(Constants.RESERVED_WORD_PROGRAM);
		
	}

	public int getNextState(int currentState, char currentChar) {
		int returnState = Constants.ERR_STATE;
		Pair<Integer, Character> pairToGet = null;
		Pair<Integer, Character> newPair = new Pair<Integer, Character>(currentState, currentChar);
//		System.out.println("New pair: " + newPair);
		for(Pair<Integer, Character> pair:stateTransitionTable.keySet()){
			if(pair.equals(newPair)){
				pairToGet = pair;
				break;
			}
		}
		if(!stateTransitionTable.containsKey(pairToGet)){
//			System.out.println("Next state do no exist for pair: " + pairToGet);
			if(currentState == Constants.SINGLE_COMMENT_STATE){
				returnState = Constants.SINGLE_COMMENT_STATE;
			} else if(currentState == Constants.MULTI_COMMENT_STATE1){
				returnState = Constants.MULTI_COMMENT_STATE1;
			}else if(currentState == Constants.MULTI_COMMENT_STATE2){
				returnState = Constants.MULTI_COMMENT_STATE1;
			}
		} else {
			returnState = stateTransitionTable.get(pairToGet);
		}
		return returnState;
	}
	
	public String getFinalState(int state) {
		return finalStates.get(state);
	}
	
	public boolean isFinalState(int state) {
		return finalStates.containsKey(state);
	}
	
	public boolean isReservedWord(String token) {
		boolean contains = false;
		for(String word:reservedWords){
			if(word.equalsIgnoreCase(token)){
				contains = true;
				break;
			}
		}
		return contains;
	}
}
