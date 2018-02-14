package model.genetic;

import controller.Darwini;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static model.genetic.RefactoGeneticAlgorithm.ROBOT_DIRECTORY;

public class Population {

    /*	----- PATHS -----	*/

    /**
     * <p>
     * The directory where the population will be saved.
     * </p>
     */
    static final String POPULATION_DIRECTORY = "data/population/";

    /**
     * <p>
     * The name of an XML individual file.
     * </p>
     */
    static final String INDIVIDUAL_FILENAME = "Individual";

    /*	----- ATTRIBUTES -----	*/

    /**
     * <p>
     * The array of individuals contained in the population.
     * </p>
     */
    private ArrayList<Individual> individuals;

    /**
     * <p>
     * The index of the current generation.
     * </p>
     */
    private int generation;

    /**
     * <p>
     * The number of individuals in the population.
     * </p>
     */
    private int size;

    /**
     * <p>
     * The number of individuals that will be kept from one generation to another.
     * </p>
     */
    private int nbSurvivors;



    /*	----- CONSTRUCTOR -----	*/

    /**
     * <p>
     * Creates the population at generation 1, initializes and randomizes every individual.
     * </p>
     */
    public Population(int s, int nbSurv) {

        generation = 1;
        nbSurvivors = nbSurv;
        size = s;

        individuals = new ArrayList<Individual>(s);

        try{
            createDirs();
        } catch(IOException e){
            e.printStackTrace();
        }

        for (int i = 1; i <= size; i++) {
            individuals.add(new Individual(i));
        }

    }

    /*	----- METHODS -----	*/

    /**
     * <p>
     * Makes every individual fight to determine its fitness.
     * </p>
     */
    public void makeFight() {

        System.out.println("Making generation " + generation + " fight...");

        for (Individual ind : individuals) {
            ind.fight();
        }

        System.out.println("Average fitness of generation " + generation + " : " + averageFitness());

    }

    /**
     * <p>
     * Determines the strongest individuals, kills the weaklings, then creates the next generation with the surviving
     * individuals and their childs.
     * </p>
     */
    public void nextGeneration() {

        System.out.println("Generating generation " + (generation + 1) + "...");

        //sortIndividuals();
        Collections.sort(individuals);
        Collections.reverse(individuals);

        saveBest();

        killWeaklings();

        loadBest();

        generateChildren();

        generation ++;

        System.out.println("Done !");

    }

    /**
     * <p>
     * Saves the best individuals in temporary files.
     * </p>
     */
    private void saveBest() {

        for (int i = 1; i <= nbSurvivors; i++) {
            try {
                copyFile(POPULATION_DIRECTORY + INDIVIDUAL_FILENAME + i + ".xml",
                        POPULATION_DIRECTORY + "Temp_" + INDIVIDUAL_FILENAME + i + ".xml");
            } catch (IOException e) {
                e.printStackTrace();
            }
            individuals.get(i - 1).setIndex(i);
        }

    }

    /**
     * <p>
     * Deletes all individual files (except for the temporary ones) and every "weak" Individual in the list.
     * </p>
     */
    private void killWeaklings() {

        File f;

        for (int i = 1; i <= size; i++) {
            f = new File(POPULATION_DIRECTORY + INDIVIDUAL_FILENAME + i + ".xml");
            try {
                if (!f.delete()){
                    System.out.println("An error occured when trying to delete file " + f.getName());
                }
            } catch (SecurityException e){
                System.out.println("You don't have the permission to delete the file " + f.getName());
                e.printStackTrace();
            }
        }

        for (int i = size - 1; i >= nbSurvivors; i--) {
            individuals.remove(i);
        }

    }

    /**
     * <p>
     * Realize a mutation on a matrix
     * </p>
     *
     * @param m the matrix to mutate
     */


    /**
     * <p>
     * Make a mutation on an individual.
     * </p>
     *
     * @param child the individual to mutate
     */




    /**
     * <p>
     * Reloads the best individuals.
     * </p>
     */
    private void loadBest() {

        File f = null;

        for (int i = 1; i <= nbSurvivors; i++) {
            f = new File(POPULATION_DIRECTORY + "Temp_" + INDIVIDUAL_FILENAME + i + ".xml");
            try{
                if (!f.renameTo(new File(POPULATION_DIRECTORY + INDIVIDUAL_FILENAME + i + ".xml"))){
                    System.out.println("An error occured when trying to rename the file " + f.getName());
                }
            } catch (SecurityException e){
                System.out.println("You don'y have the permission to rename the file " + f.getName());
                e.printStackTrace();
            }
        }

    }

    /**
     * <p>
     * Fills the new generation with childs of the surviving individuals.
     * </p>
     */
    private void generateChildren() {

        int motherID, fatherID;

        for (int i = nbSurvivors + 1; i <= size; i++) {

            motherID = random(0, nbSurvivors);
            do {
                fatherID = random(0, nbSurvivors);
            } while (motherID == fatherID);

            individuals.add(new Individual(i, individuals.get(motherID).getPerceptron(),
                    individuals.get(fatherID).getPerceptron()));
        }

    }

    /**
     * <p>
     * Return the best individual.
     * </p>
     */
    public Individual bestIndividual() {

        Collections.sort(individuals);
        Collections.reverse(individuals);

        try {
            copyFile(RefactoGeneticAlgorithm.POPULATION_DIRECTORY + RefactoGeneticAlgorithm.INDIVIDUAL_FILENAME
                    + 1 + ".xml", ROBOT_DIRECTORY + Darwini.PERCEPTRON_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return individuals.get(0);

    }

    /**
     * <p>
     * Return the average fitness of the generation.
     * </p>
     */
    public double averageFitness() {

        double avg = 0;

        for (Individual ind : individuals) {
            avg += ind.getFitness();
        }

        return (avg / size);

    }



    /*	----- UTIL -----	*/

    /**
     * <p>
     * Copy a file from a path to another path.
     * </p>
     *
     * @param inputFile  the source file
     * @param outputFile the destination file
     * @throws IOException if the file paths do not exist
     */
    private void copyFile(String inputFile, String outputFile) throws IOException {
        // Copy the tested perceptron in the specified directory
        FileInputStream is = new FileInputStream(inputFile);
        FileOutputStream os = new FileOutputStream(outputFile);

        byte[] buffer = new byte[1024];
        int length;

        while ((length = is.read(buffer)) > 0)
            os.write(buffer, 0, length);

        is.close();
        os.close();
        ;
    }

    /**
     * <p>
     * Return a random number between min and max.
     * </p>
     *
     * @param min the minimum value
     * @param max the maximum value
     * @return the random number
     */
    private int random(int min, int max) {
        return (int) (Math.random() * (max - min)) + min;
    }


    /**
     * Creates the directories for population and robots
     * @throws IOException if the system can't create these directories
     */
    private void createDirs() throws IOException{
        File f = new File(POPULATION_DIRECTORY);
        if (!f.exists()){
            if (!f.mkdir()){
                throw new IOException("Unable to create the dir " + POPULATION_DIRECTORY);
            }
        }

        f = new File(ROBOT_DIRECTORY);
        if (!f.exists()){
            if (!f.mkdir()){
                throw new IOException("Unable to create the dir " + ROBOT_DIRECTORY);
            }
        }
    }

    private void sortIndividuals(){
        individuals.sort(new IndividualsComparator());
    }


    /**
     * Custom comparator to sort the list of individuals.
     */
    private static class IndividualsComparator implements Comparator<Individual> {

        @Override
        public int compare(Individual individual, Individual t1) {
            return individual.compareTo(t1);
        }
    }


}
