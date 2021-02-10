import csv.CSVParser;
import csv.CSVRecord;
import csv.SEFileUtil;

import java.io.File;

public class BirthStatistics {

    public final String pathToDirCSVs;

    public BirthStatistics (String pathCSVs){
        pathToDirCSVs = pathCSVs;
    }

    /**
     * This method returns the path to the CSV file of the specified year
     * @param year
     * @return
     */
    private String getPathToCSV (int year){
        File[] csvFiles = new File (pathToDirCSVs).listFiles();
        for (File csvF : csvFiles){
            if (csvF.getName().contains(Integer.toString(year))){
                return csvF.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * This method returns the row number in the CSV file of the most popular name by the given gender
     * @param year
     * @param gender
     * @return
     */
    private int getCsvRowOfMostPopularNameByGender(int year, String gender){
        int rank = -1;
        SEFileUtil seFileUtil = new SEFileUtil(getPathToCSV(year));
        for (CSVRecord record : seFileUtil.getCSVParser()) {
            String currGender = record.get(1);
            if (currGender.equals(gender)){
                rank = (int) record.getRecordNumber();
                break;
            }
        }
        return rank;
    }

    /*
     * This method returns the total number of births males and females in a file
     */
    public void totalBirths (int year) {
        int totalBirths = 0;
        int totalBoys = 0;
        int totalGirls = 0;
        SEFileUtil seFileUtil = new SEFileUtil(getPathToCSV(year));
        for (CSVRecord rec : seFileUtil.getCSVParser()) {
            int numBorn = Integer.parseInt(rec.get(2));
            totalBirths += numBorn;
            if (rec.get(1).equals("M")) {
                totalBoys += numBorn;
            }
            else {
                totalGirls += numBorn;
            }
        }
        System.out.println("total births = " + totalBirths);
        System.out.println("female girls = " + totalGirls);
        System.out.println("male boys = " + totalBoys);
    }

    /*
     * This method returns the rank of the name in the file for the given gender,
     * where rank 1 is the name with the largest number of births.
     * If the name is not in the file, then -1 is returned.
     */
    public int getRank(int year, String name, String gender) {
        int rank = -1;
        int rankOfFirstGender = getCsvRowOfMostPopularNameByGender(year,gender);
        SEFileUtil fr = new SEFileUtil(getPathToCSV(year));
        CSVParser parser = fr.getCSVParser();
        for(CSVRecord record : parser) {
            String currName = record.get(0);
            String currGender = record.get(1);
            if(currGender.equals(gender) && currName.equals(name)) {
                rank = (int)record.getRecordNumber()-rankOfFirstGender+1;
                break;
            }
        }
        return rank;
    }

    /*
     * This method returns the name of the person in the file at this rank,
     * for the given gender, where rank 1 is the name with the largest number of births.
     * If the rank does not exist in the file, then “NO NAME” is returned.
     */
    public String getName(int year, int rank, String gender) {
        String name = "";
        int rankOfFirstGender = getCsvRowOfMostPopularNameByGender(year,gender);
        SEFileUtil fr = new SEFileUtil(getPathToCSV(year));
        CSVParser parser = fr.getCSVParser();
        for(CSVRecord record : parser) {
            long currRank = record.getRecordNumber();
            String currGender = record.get(1);
            String currName = record.get(0);
            if(currRank == (rankOfFirstGender+rank-1) && currGender.equals(gender)) {
                name = currName;
            }
        }

        if(name != "") {
            return name;
        }
        else {
            return "NO NAME";
        }
    }

    /*
     * This method selects a range of files to process and returns an integer,
     * the year with the highest rank for the name and gender.
     * If the name and gender are not in any of the selected files, it should return -1.
     */
    public int yearOfHighestRank(int yearStart, int yearEnd, String name, String gender) {
        long highestRank = Integer.MAX_VALUE;
        int yearOfHighestRank = -1;

        // Iterate through the years
        for(int y = yearStart; y < yearEnd; y++) {
            SEFileUtil seFileUtil = new SEFileUtil(getPathToCSV(y));
            CSVParser parser = seFileUtil.getCSVParser();
            int rankOfFirstGender = getCsvRowOfMostPopularNameByGender(y,gender);

            // Iterate through all records in file
            for(CSVRecord record : parser) {
                String currName = record.get(0);
                String currGender = record.get(1);

                if(currName.equals(name) && currGender.equals(gender)) {
                    long currRank = record.getRecordNumber()-rankOfFirstGender+1;

                    if(highestRank > currRank) {
                        highestRank = currRank;
                        yearOfHighestRank = y;
                    }
                }
            }
        }
        return yearOfHighestRank;
    }

    /*
     * This method returns the average rank of a name in multiple files
     */
    public double getAverageRank(int yearStart, int yearEnd, String name, String gender) {
        // Define rankTotal, howMany
        double rankTotal = 0.0;
        int howMany = 0;
        // For every file the directory add name rank to agvRank
        for(int y = yearStart; y <= yearEnd; y++){
            SEFileUtil seFileUtil = new SEFileUtil(getPathToCSV(y));
            CSVParser parser = seFileUtil.getCSVParser();
            int rankOfFirstGender = getCsvRowOfMostPopularNameByGender(y,gender);
            for(CSVRecord record : parser) {
                String currName = record.get(0);
                String currGender = record.get(1);
                if(currName.equals(name) && currGender.equals(gender)){
                    long currRank = record.getRecordNumber()-rankOfFirstGender+1;
                    rankTotal += (double)currRank;
                    howMany += 1;
                    break;
                }
            }
        }
        // Define avgRank = rankTotal / howMany
        return howMany == 0? -1 : (rankTotal / (double)howMany);
    }

    /*
     * This method returns the total births of the same gender that are ranked higher
     * than the parameter name
     */
    public int getTotalBirthsRankedHigher(int year, String name, String gender) {
        int numBorn = 0;
        long rank = getRank(year, name, gender);
        SEFileUtil seFileUtil = new SEFileUtil(getPathToCSV(year));
        CSVParser parser = seFileUtil.getCSVParser();
        //finds rank of the most popular name by the given gender
        int rankOfFirstGender = getCsvRowOfMostPopularNameByGender(year,gender);
        for(CSVRecord record : parser) {
            int currBorn = Integer.parseInt(record.get(2));
            String currGender = record.get(1);
            long currRank = record.getRecordNumber()-rankOfFirstGender+1;
            if(gender.equals(currGender) && ((rank > currRank) || rank == -1) ) {
                numBorn += currBorn;
            }
        }
        return numBorn;
    }

    public static void main(String[] args) {
        BirthStatistics birthStatistics = new BirthStatistics(args[0]);
        birthStatistics.totalBirths(2010);
        int rank = birthStatistics.getRank(2010, "Asher", "M");
        System.out.println("Rank is: " + rank);
        String name = birthStatistics.getName(2012, 10, "M");
        System.out.println("Name: " + name);
        System.out.println(birthStatistics.yearOfHighestRank(1880, 2014,"David", "M"));
        System.out.println(birthStatistics.yearOfHighestRank(1880, 2014,"Jennifer", "F"));
        System.out.println(birthStatistics.getAverageRank(1880, 2014, "Benjamin", "M"));
        System.out.println(birthStatistics.getAverageRank(1880,2014, "Lois", "F"));
        System.out.println(birthStatistics.getTotalBirthsRankedHigher(2014, "Draco", "M"));
        System.out.print(birthStatistics.getTotalBirthsRankedHigher(2014, "Sophia", "F"));
    }


}
