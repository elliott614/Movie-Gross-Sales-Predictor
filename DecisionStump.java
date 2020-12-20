import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

public class DecisionStump {
	private static final String DATA_PATH = "./movie_metadata.csv";
	private static final String OUTPUT_PATH = "./output.txt";
	private static final int REVENUE_CUTOFF = 10000000;
	private static final int K_BUDGET = 6; // split each feature into K categories
	private static final int K_DURATION = 4;
	private static final int K_VOTE_AVERAGE = 5;
	private static final int K_VOTE_COUNT = 5;
	private static final int NUM_GENRES = 10; // hard-coded
//	private static final Double ACTION = 0.0;
//	private static final Double ADVENTURE = 1.0;
//	private static final Double HORROR = 2.0;
//	private static final Double DOCUMENTARY = 3.0;
//	private static final Double DRAMA = 4.0;
//	private static final Double CRIME = 5.0;
//	private static final Double BIOGRAPHY = 6.0;
//	private static final Double FANTASY = 7.0;
//	private static final Double FAMILY = 8.0;
//	private static final Double COMEDY = 9.0;

	public static void main(String[] args) {
		try {
			System.out.println("Parsing Movie Data");
			MovieData md = new MovieData(DATA_PATH, REVENUE_CUTOFF);
			System.out.println("Categorizing");
			md.categorize(K_BUDGET, K_DURATION, K_VOTE_AVERAGE, K_VOTE_COUNT);

//			md.printFirstGenreCounts();

			// Calculate information gain and counts for Decision Stumps

			System.out.println("Calculating Entropy");
			// calculate entropy
			Double entropy = 0.0;
			int numInstances = md.movies.size();
			int numPositive = 0;
			int numNegative = 0;
			for (int i = 0; i < numInstances; i++) {
				if (md.y.get(i) == 1.0)
					numPositive++;
				else
					numNegative++;
			}
			entropy -= Double.valueOf(numNegative) / numInstances * Math.log(Double.valueOf(numNegative) / numInstances)
					/ Math.log(2);
			entropy -= (0.0 + numPositive) / numInstances * Math.log((0.0 + numPositive) / numInstances) / Math.log(2);

//			System.out.println("entropy: " + entropy + " numNegative: " + numNegative + " numPositive: " + numPositive
//					+ " numInstances: " + numInstances);

			System.out.println("Calculating Information Gains");
			// Start with budget
			int[] numPositiveXjBudget = new int[K_BUDGET];
			int[] numNegativeXjBudget = new int[K_BUDGET];
			int[] numXjBudget = new int[K_BUDGET];
			Double informationGainBudget = calcInfGain(entropy, K_BUDGET, numInstances, md, numXjBudget,
					numPositiveXjBudget, numNegativeXjBudget, 0);

			// Duration
			int[] numPositiveXjDuration = new int[K_DURATION];
			int[] numNegativeXjDuration = new int[K_DURATION];
			int[] numXjDuration = new int[K_DURATION];
			Double informationGainDuration = calcInfGain(entropy, K_DURATION, numInstances, md, numXjDuration,
					numPositiveXjDuration, numNegativeXjDuration, 1);

			// VoteAverage
			int[] numPositiveXjVA = new int[K_VOTE_AVERAGE];
			int[] numNegativeXjVA = new int[K_VOTE_AVERAGE];
			int[] numXjVA = new int[K_VOTE_AVERAGE];
			Double informationGainVA = calcInfGain(entropy, K_VOTE_AVERAGE, numInstances, md, numXjVA, numPositiveXjVA,
					numNegativeXjVA, 2);

			// VoteCount
			int[] numPositiveXjVC = new int[K_VOTE_COUNT];
			int[] numNegativeXjVC = new int[K_VOTE_COUNT];
			int[] numXjVC = new int[K_VOTE_COUNT];
			Double informationGainVC = calcInfGain(entropy, K_VOTE_COUNT, numInstances, md, numXjVC, numPositiveXjVC,
					numNegativeXjVC, 3);

			// Genres
			int[] numPositiveXjGenre = new int[NUM_GENRES];
			int[] numNegativeXjGenre = new int[NUM_GENRES];
			int[] numXjGenre = new int[NUM_GENRES];
			Double informationGainGenre = calcInfGain(entropy, NUM_GENRES, numInstances, md, numXjGenre,
					numPositiveXjGenre, numNegativeXjGenre, 4);

			// Time to generate output file
			BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_PATH));
			// Budget
			bw.write("" + K_BUDGET + ", " + informationGainBudget + ", " + numPositiveXjBudget[0] + ", "
					+ numNegativeXjBudget[0]);
			for (int i = 1; i < K_BUDGET; i++) {
				bw.write(", " + numPositiveXjBudget[i] + ", " + numNegativeXjBudget[i]);
			}
			bw.newLine();

			// Genre
			bw.write("" + NUM_GENRES + ", " + informationGainGenre + ", " + numPositiveXjGenre[0] + ", "
					+ numNegativeXjGenre[0]);
			for (int i = 1; i < NUM_GENRES; i++) {
				bw.write(", " + numPositiveXjGenre[i] + ", " + numNegativeXjGenre[i]);
			}
			bw.newLine();

			// Duration
			bw.write("" + K_DURATION + ", " + informationGainDuration + ", " + numPositiveXjDuration[0] + ", "
					+ numNegativeXjDuration[0]);
			for (int i = 1; i < K_DURATION; i++) {
				bw.write(", " + numPositiveXjDuration[i] + ", " + numNegativeXjDuration[i]);
			}
			bw.newLine();

			// VoteAverage
			bw.write("" + K_VOTE_AVERAGE + ", " + informationGainVA + ", " + numPositiveXjVA[0] + ", "
					+ numNegativeXjVA[0]);
			for (int i = 1; i < K_VOTE_AVERAGE; i++) {
				bw.write(", " + numPositiveXjVA[i] + ", " + numNegativeXjVA[i]);
			}
			bw.newLine();

			// VoteCount
			bw.write("" + K_VOTE_COUNT + ", " + informationGainVC + ", " + numPositiveXjVC[0] + ", "
					+ numNegativeXjVC[0]);
			for (int i = 1; i < K_VOTE_COUNT; i++) {
				bw.write(", " + numPositiveXjVC[i] + ", " + numNegativeXjVC[i]);
			}
			bw.close();

			System.out.println("Done");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Double calcInfGain(Double entropy, int K, int N, MovieData md, int[] numXj, int[] numPositiveXj,
			int[] numNegativeXj, int feature) {

		for (int i = 0; i < N; i++) {
			Movie cur = md.movies.get(i);
			Double y = md.y.get(i);
			int tmp = 0;

			switch (feature) {
			case (0):
				tmp = (int) Math.round(cur.budgetCategory);
				break;
			case (1):
				tmp = (int) Math.round(cur.durationCategory);
				break;
			case (2):
				tmp = (int) Math.round(cur.voteAverageCategory);
				break;
			case (3):
				tmp = (int) Math.round(cur.voteCountCategory);
				break;
			case (4):
				tmp = (int) Math.round(cur.genreCategory);
				break;
			default:
				tmp = 0;
			}

			numXj[tmp]++;
			if (y == 1.0)
				numPositiveXj[tmp]++;
			else
				numNegativeXj[tmp]++;
		}

		Double conditionalEntropy = 0.;

		for (int i = 0; i < K; i++) {
			if (numNegativeXj[i] != 0)
				conditionalEntropy -= (0.0 + numNegativeXj[i]) / N * Math.log((0.0 + numNegativeXj[i]) / numXj[i])
						/ Math.log(2);
			if (numPositiveXj[i] != 0)
				conditionalEntropy -= (0.0 + numPositiveXj[i]) / N * Math.log((0.0 + numPositiveXj[i]) / numXj[i])
						/ Math.log(2);
		}

		return entropy - conditionalEntropy;
	}

}

class Movie {
	long budget;
	String[] genres;
	int duration;
	Double voteAverage; // imdb_score
	int voteCount; // num_voted_users

	// categories to use for decision tree
	Double budgetCategory = 0.;
	Double genreCategory = 0.;
	Double durationCategory = 0.;
	Double voteAverageCategory = 0.;
	Double voteCountCategory = 0.;

	Movie(long budget, String[] genres, int duration, Double voteAverage, int voteCount) {
		this.budget = budget;
		this.genres = genres;
		this.duration = duration;
		this.voteAverage = voteAverage;
		this.voteCount = voteCount;
	}
}

// Constructor also parses .csv file
class MovieData {
	List<Movie> movies;
	List<Double> y;

	MovieData(String path, int revenueCutoff) throws FileNotFoundException, IOException {
		this.movies = new ArrayList<Movie>();
		this.y = new ArrayList<Double>();
		// indexes
		int BUDGET = 0;
		int GENRES = 0;
		int DURATION = 0;
		int VOTE_AVERAGE = 0;
		int VOTE_COUNT = 0;
		int REVENUE = 0;
		int COUNTRY = 0; // movies not in English may have budgets that aren't in USD. Do not include.
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String nxtLine;
			String[] line;
			line = br.readLine().split(",");
			// find indexes of each type of data
			for (int i = 0; i < line.length; i++) {
				String token = line[i];
				switch (token) {
				case "budget":
					BUDGET = i;
					break;
				case "genres":
					GENRES = i;
					break;
				case "duration":
					DURATION = i;
					break;
				case "imdb_score":
					VOTE_AVERAGE = i;
					break;
				case "num_voted_users":
					VOTE_COUNT = i;
					break;
				case "gross":
					REVENUE = i;
					break;
				case "country":
					COUNTRY = i;
					break;
				}
			}
			for (; ((nxtLine = br.readLine()) != null);) {
				// regExpr for split at commas, except when one is followed by odd number of "s
				line = nxtLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
				if (line[COUNTRY].equals("USA") && !(line[BUDGET].equals("") || line[GENRES].equals("")
						|| line[DURATION].equals("") || line[VOTE_AVERAGE].equals("") || line[VOTE_COUNT].equals("")
						|| line[REVENUE].equals(""))) {
					this.movies.add(new Movie(Long.parseLong(line[BUDGET]), line[GENRES].split("[|]"),
							Integer.parseInt(line[DURATION]), Double.parseDouble(line[VOTE_AVERAGE]),
							Integer.parseInt(line[VOTE_COUNT])));
					this.y.add(Integer.parseInt(line[REVENUE]) <= revenueCutoff ? 0.0 : 1.0);
				}
			}
		}
	}

	// Assigns values to the category variables in each Movie object
	public void categorize(int K_BUDGET, int K_DURATION, int K_VOTE_AVERAGE, int K_VOTE_COUNT) {
		// enumerate genres
		Double ACTION = 0.0;
		Double ADVENTURE = 1.0;
		Double HORROR = 2.0;
		Double DOCUMENTARY = 3.0;
		Double DRAMA = 4.0;
		Double CRIME = 5.0;
		Double BIOGRAPHY = 6.0;
		Double FANTASY = 7.0;
		Double FAMILY = 8.0;
		Double COMEDY = 9.0;

		// Calculate mins/maxes for numerical features
		long minBudget = 99999999999L; // something big
		long maxBudget = 0;
		int BUDGET_CEILING = 150000000; // to create more even split in budget category
		int BUDGET_FLOOR = 1000000;
		int minDuration = 99999;
		int maxDuration = 0;
		int DURATION_CEILING = 250; // to split duration categories bettedr
		Double minVoteAverage = 100.0;
		Double maxVoteAverage = 0.0; // let there be fewer movies in the worst vote average category
		int minVoteCount = 99999;
		int maxVoteCount = 0;
		int VOTE_COUNT_CEILING = 600000; // pre-processing as above

		for (int i = 0; i < this.movies.size(); i++) {
			Movie curr = this.movies.get(i);
			if (curr.budget < minBudget && curr.budget > BUDGET_FLOOR)
				minBudget = curr.budget;
			if (curr.budget > maxBudget && curr.budget <= BUDGET_CEILING)
				maxBudget = curr.budget;
			if (curr.duration < minDuration)
				minDuration = curr.duration;
			if (curr.duration > maxDuration && curr.duration <= DURATION_CEILING)
				maxDuration = curr.duration;
			if (curr.voteAverage < minVoteAverage)
				minVoteAverage = curr.voteAverage;
			if (curr.voteAverage > maxVoteAverage)
				maxVoteAverage = curr.voteAverage;
			if (curr.voteCount < minVoteCount)
				minVoteCount = curr.voteCount;
			if (curr.voteCount > maxVoteCount && curr.voteCount <= VOTE_COUNT_CEILING)
				maxVoteCount = curr.voteCount;
		}

		// now calculate category variables and store inside Movie objects
		for (int i = 0; i < this.movies.size(); i++) {
			Movie curr = this.movies.get(i);
			curr.budgetCategory = curr.budget < BUDGET_CEILING
					? (curr.budget > BUDGET_FLOOR
							? Math.floor((curr.budget - minBudget) / ((0.01 + maxBudget - minBudget) / K_BUDGET))
							: 0.0)
					: 0. + K_BUDGET - 1;
			curr.durationCategory = curr.duration < DURATION_CEILING
					? Math.floor((curr.duration - minDuration) / ((0.01 + maxDuration - minDuration) / K_DURATION))
					: 0. + K_DURATION - 1;
			curr.voteAverageCategory = Math.floor(
					(curr.voteAverage - minVoteAverage) / ((0.01 + maxVoteAverage - minVoteAverage) / K_VOTE_AVERAGE));
			curr.voteCountCategory = curr.voteCount < VOTE_COUNT_CEILING
					? Math.floor(
							(curr.voteCount - minVoteCount) / ((0.01 + maxVoteCount - minVoteCount) / K_VOTE_COUNT))
					: 0. + K_VOTE_COUNT - 1;
			switch (curr.genres[0]) {
			case ("Action"):
				curr.genreCategory = ACTION;
				break;
			case ("Adventure"):
				curr.genreCategory = ADVENTURE;
				break;
			case ("Horror"):
				curr.genreCategory = HORROR;
				break;
			case ("Romance"):
				curr.genreCategory = DRAMA;
				break;
			case ("Western"): // combine Western into Action
				curr.genreCategory = ACTION;
				break;
			case ("Documentary"):
				curr.genreCategory = DOCUMENTARY;
				break;
			case ("Sci-Fi"): // combine Sci-Fi into Fantasy
				curr.genreCategory = FANTASY;
				break;
			case ("Drama"):
				curr.genreCategory = DRAMA;
				break;
			case ("Thriller"): // combine Horror into Thriller
				curr.genreCategory = HORROR;
				break;
			case ("Crime"):
				curr.genreCategory = CRIME;
				break;
			case ("Biography"):
				curr.genreCategory = BIOGRAPHY;
				break;
			case ("Fantasy"):
				curr.genreCategory = FANTASY;
				break;
			case ("Animation"): // combine Animation into Family
				curr.genreCategory = FAMILY;
				break;
			case ("Family"):
				curr.genreCategory = FAMILY;
				break;
			case ("Comedy"):
				curr.genreCategory = COMEDY;
				break;
			case ("Mystery"): // combine Crime into Mystery
				curr.genreCategory = CRIME;
				break;
			case ("Musical"): // combine Musical into Family
				curr.genreCategory = FAMILY;
				break;
			default:
				curr.genreCategory = 0.0;
			}
		}

	}

	// prints the counts of the first listed genre to help make decision regarding
	// genres to combine
	public void printFirstGenreCounts() {
		Map<String, Integer> genreMap = new HashMap<String, Integer>(20);
		// fill hash map
		for (int i = 0; i < this.movies.size(); i++) {
			Movie curr = this.movies.get(i);
			// if (!genreMap.replace(curr.genres[0], genreMap.get(curr.genres[0]) + 1))
			// genreMap.put(curr.genres[0], 0);
			if (genreMap.containsKey(curr.genres[0]))
				genreMap.put(curr.genres[0], 1 + genreMap.get(curr.genres[0]));
			else
				genreMap.put(curr.genres[0], 1);
		}
		// print hash map
		Set<String> keys = genreMap.keySet();
		Iterator<String> kIter = keys.iterator();
		while (kIter.hasNext()) {
			String k = kIter.next();
			System.out.println(k + ": " + genreMap.get(k));
		}

	}

}