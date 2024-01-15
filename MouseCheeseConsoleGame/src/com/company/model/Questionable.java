package com.company.model;

public interface Questionable {
    /**
     * Method that returns the question for this element.
     * @return The question to ask to the player.
     */
    public String getQuestion();

    /**
     * Method that decides if the submitted answer is right
     * @param answer
     * @return false if the answer is wrong, true if the answer is right.
     */
    public boolean submitAnswer(String answer);
}
