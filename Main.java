import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        //PARAMETERS
        final int MAX_EPOCHS = 2000;
        final double TEMP = 0.1;
        Random rand = new Random(1); //random object with SEED!

        //INITIALIZATIONS
        int i1;
        int i2;
        Integer temp;
        int best_score = 0;
        int new_score = 0;

        /* ______INPUT_______ */
        System.out.println("STARTING!");
        //String fileName = "a_example.txt";
        //String resultFile = "a.txt";
        //String fileName = "b_read_on.txt";
        //String resultFile = "b.txt";
        String fileName = "c_incunabula.txt";
        String resultFile = "c.txt";
        //String fileName = "d_tough_choices.txt";
        //String resultFile = "d.txt";
        //String fileName = "e_so_many_books.txt";
        //String resultFile = "e.txt";
        //String fileName = "f_libraries_of_the_world.txt";
        //String resultFile = "f.txt";
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int nBooks = scanner.nextInt();
        int nLibraries = scanner.nextInt();
        int nDays = scanner.nextInt();
        ArrayList<Integer> bookScores = new ArrayList<>();
        for (int i=0; i<nBooks;i++)
            bookScores.add(scanner.nextInt());
        ArrayList<ArrayList<Integer>> libInfo = new ArrayList();
        for (int i=0; i<nLibraries;i++){
            ArrayList<Integer> library = new ArrayList<>();
            int aux1 = scanner.nextInt();
            library.add(aux1);
            library.add(scanner.nextInt());
            library.add(scanner.nextInt());
            for (int j=0; j<aux1; j++){
                library.add(scanner.nextInt());
            }
            libInfo.add(library);
        }

        ArrayList<Integer> bestOrder = new ArrayList<>();
        ArrayList<Integer> new_order = new ArrayList<>();

        ArrayList<Integer> readBooks;
        ArrayList<Integer> originBooks;
        readBooks = new ArrayList<>();
        originBooks = new ArrayList<>();

        ArrayList<Integer> bestReadBooks = new ArrayList<>();
        ArrayList<Integer> bestOriginBooks = new ArrayList<>();

        ArrayList<Integer> orderedList = null;


        for(int epoch=1;epoch<MAX_EPOCHS; epoch++){

            System.out.println(((double) epoch)/MAX_EPOCHS*100 + "%");

            if(epoch==1){
                for (int i = 0; i < nLibraries; i++) { //create array from 0...num_libraries
                    bestOrder.add(i);
                }

                Collections.shuffle(bestOrder, rand); //shuffle the list

                bestReadBooks = new ArrayList<>();
                bestOriginBooks = new ArrayList<>();

                best_score = calcScore(nDays, bestOrder, libInfo, bookScores, bestReadBooks, bestOriginBooks);
            }else {
                //We introduce random swap
                i1 = rand.nextInt(nLibraries);
                i2 = rand.nextInt(nLibraries);
                while(i1==i2) {
                    i2 = rand.nextInt(nLibraries);
                }

                new_order = (ArrayList<Integer>) bestOrder.clone();

                temp = new_order.get(i1);
                new_order.set(i1, new_order.get(i2));
                new_order.set(i2, temp);

                //Compute books scanning and score using new_order
                readBooks = new ArrayList<>();
                originBooks = new ArrayList<>();

                orderedList = (ArrayList<Integer>) new_order.clone(); //to ArrayList

                new_score = calcScore(nDays, orderedList, libInfo, bookScores, readBooks, originBooks);

                //if it improves: accept the change
                if(best_score < new_score){
                    temp = bestOrder.get(i1);
                    bestOrder.set(i1, bestOrder.get(i2));
                    bestOrder.set(i2, temp);
                    best_score = new_score;

                    bestReadBooks = (ArrayList<Integer>) readBooks.clone();
                    bestOriginBooks = (ArrayList<Integer>) originBooks.clone();

                }else{
                    if(rand.nextDouble() < Math.exp((new_score - best_score)/TEMP)) {
                        temp = bestOrder.get(i1);
                        bestOrder.set(i1, bestOrder.get(i2));
                        bestOrder.set(i2, temp);
                        best_score = new_score;

                        bestReadBooks = (ArrayList<Integer>) readBooks.clone();
                        bestOriginBooks = (ArrayList<Integer>) originBooks.clone();
                    }
                }
            }
        }

        /*_____OUTPUT______*/
        System.out.println("WRITING OUTPUT");

        int nFinalBooks = bestReadBooks.size();
        int nFinalLibs;

        Set<Integer> set = new HashSet<>(bestOriginBooks);
        nFinalLibs = set.size();

        //Aqui tindrem ja el nFinalLibs
        ArrayList<ArrayList<Integer>> ord_llibres = new ArrayList<>();


        for (int i = 0; i < nLibraries; i++){
            ord_llibres.add(new ArrayList<>());
        }

        for (int i=0; i < nFinalBooks; i++){
            ord_llibres.get(bestOriginBooks.get(i)).add(bestReadBooks.get(i));
        }


        try {
            PrintWriter writer = new PrintWriter(resultFile, "UTF-8");
            writer.println(nFinalLibs);
            for (int i = 0; i < nLibraries; i++) {
                int library = bestOrder.get(i);
                if (ord_llibres.get(library).size() == 0) continue;

                writer.println(library + " " + ord_llibres.get(library).size());
                for (int j = 0; j < ord_llibres.get(library).size(); j++) {
                    writer.print(ord_llibres.get(library).get(j));
                    if (j == ord_llibres.get(library).size()-1)
                        writer.print("\n");
                    else
                        writer.print(" ");

                }

            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("FINISHED!");

    }

    /*
     * idLibs: ordered list of libraries
     */
    private static int calcScore(int days, ArrayList<Integer> idLibs, ArrayList<ArrayList<Integer>> infoLibs,
                                 ArrayList<Integer> bookScore, ArrayList<Integer> readBooks,  ArrayList<Integer> originBooks) {

        ArrayList<Integer> cumulativeDays = new ArrayList<>();
        int[] indices = new int[idLibs.size()];

        for (int i = 0; i < idLibs.size(); i++){
            indices[i] = 3;
        }

        for (int i = 0; i < idLibs.size(); i++) {
            int libActual = idLibs.get(i);
            int numDays = infoLibs.get(libActual).get(1);
            if (i>0) {
                numDays += cumulativeDays.get(i - 1);
            }
            if (numDays > days) {
                break;
            }
            cumulativeDays.add(numDays);
        }


        int lastLibraryAdded = 0;
        int cumulativeScore[] = new int[cumulativeDays.size()];

        for (int d = 0; d < days; d++) {
            // Check index at cumulative days
            if(lastLibraryAdded < cumulativeDays.size() && d == cumulativeDays.get(lastLibraryAdded)){
                lastLibraryAdded++;
            }
            // Add the libraries up to date
            for (int i = 0; i < lastLibraryAdded; i++) {
                int library = idLibs.get(i);
                cumulativeScore[i] += getScoreDate(library, infoLibs.get(library), readBooks, originBooks, bookScore, indices);
            }
        }

        int score = 0;
        for(int i = 0; i < cumulativeScore.length; i++){
            score += cumulativeScore[i];
        }

        return score;
    }

    private static int getScoreDate(int library, List<Integer> infoLibrary, ArrayList<Integer> readBooks,
                                    ArrayList<Integer> originBooks, ArrayList<Integer> bookScore, int[] indices) {
        // Get the first readable book
        int bookId = 0;
        int dayScore = 0;
        int i = 0;

        for (int d = 0; d < infoLibrary.get(2); d++) {
            boolean found = false;
            i = indices[library];
            while (!found && (i < infoLibrary.get(0))) {
                bookId = infoLibrary.get(i);
                if (!readBooks.contains(bookId)) {
                    found = true;
                }
                i++;
            }

            // save the book
            if (found) {
                readBooks.add(bookId);
                originBooks.add(library);
                dayScore += bookScore.get(bookId);
                indices[library] = i;
            } else {
                indices[library] = infoLibrary.get(0);
            }

        }
        return dayScore;
    }
}