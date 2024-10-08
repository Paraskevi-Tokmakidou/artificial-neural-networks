package com.mycompany.mlp.with.genetic.algorithms;

/**
 *
 * @author Paraskevi Tokmakidou
 */

import java.util.ArrayList;
import java.util.Random;

public class MLP {
    private final ArrayList<ArrayList<Double>> _patterns;
    private ArrayList<Double> _uniqueOutputClasses;
    private ArrayList<Double> _weights;
    private final int _count;
    private final int _nodes;
    private final int _dimension;
    private final Double _learningRate;
    private final int _maxEpoches;
    private final boolean _wantToDisplayTrainErrorInEachEpoch;

    MLP() {
        this._patterns = Data.getTrainPatterns();
        this._count = this._patterns.size();
        this._nodes = MlpConfig.getNodes();
        this._dimension = Data.getDimension();
        this._weights = new ArrayList<>();
        this._learningRate = MlpConfig.getLearning_rate();
        this._maxEpoches = MlpConfig.getMax_epoches();
        this._wantToDisplayTrainErrorInEachEpoch = false;

        System.out.println("Count of patterns: " + this._count);
        System.out.println("Dimension: " + this._dimension);
        System.out.println("Nodes: " + this._nodes);
    }

    private void initializeRandomWeights(Integer countOfWeightsDimension) {
        Random random = new Random();
        Double min = MlpConfig.getMin_value();
        Double max = MlpConfig.getMax_value();

        for (int i = 0; i < countOfWeightsDimension; i++) {
            Double randomValue = min + (max - min) * random.nextDouble(); // [min, max]
            this._weights.add(randomValue);
        }
    }

    public void initializeWeights(Boolean geneticOption, GENETIC_CROSSOVER_OPTIONS geneticCrossoverOption) {
        int countOfWeightsDimension = (this._dimension + 2) * this._nodes;
        System.out.println("Count of weights: " + countOfWeightsDimension);

        if (geneticOption) {
            GeneticAlgorithm ga = new GeneticAlgorithm(countOfWeightsDimension, geneticCrossoverOption);
            this._weights = ga.getBestChromosome();
        } else {
            this.initializeRandomWeights(countOfWeightsDimension);
        }
    }

    private void findUniqueClasses() {
        this._uniqueOutputClasses = new ArrayList<>();

        for (int i = 0; i < this._patterns.size(); i++) {
            if (this._uniqueOutputClasses.isEmpty()) {
                this._uniqueOutputClasses.add(this._patterns.get(i).get(this._dimension));
            } else {
                if (this._uniqueOutputClasses.indexOf(this._patterns.get(i).get(this._dimension)) == -1) {
                    this._uniqueOutputClasses.add(this._patterns.get(i).get(this._dimension));
                }
            }
        }

        System.out.println("Unique classes: " + _uniqueOutputClasses);
    }

    public Double getOutput(ArrayList<Double> pattern) {
        Double output = 0.0;

        for (int i = 1; i < this._nodes; i++) {

            Double innerSum = 0.0;
            for (int j = 1; j < this._dimension; j++) {
                innerSum += pattern.get(j)
                        * this._weights.get((this._dimension + 2) * (i) - (this._dimension + 1) + (j))
                        + this._weights.get((this._dimension + 2) * (i));
            }

            output += this._weights.get((this._dimension + 2) * (i) - (this._dimension + 1))
                    * MathematicalFunctions.sig(innerSum);
        }

        return output;
    }

    public Double getTrainError(Double wanted_ouput, Double output) { // E
        return ((wanted_ouput - output) * (wanted_ouput - output));
    }

    public Double calculateMseWithoutLearning(ArrayList<ArrayList<Double>> patterns) {
        if (patterns == null) {
            return -1.0;
        }

        Double sum = 0.0;

        for (int i = 0; i < patterns.size(); i++) {
            Double wanted_output = patterns.get(i).get(this._dimension);
            Double output = getOutput(patterns.get(i));
            sum += this.getTrainError(wanted_output, output);
        }

        return (sum / this._patterns.size());
    }

    private ArrayList<Double> getPatternDeriv(ArrayList<Double> pattern) {
        ArrayList<Double> patternDeriv = new ArrayList<>();
        this._weights.forEach(_item -> {
            patternDeriv.add(0.0);
        });

        for (int i = 0; i < this._nodes; i++) {
            Double arg = 0.0;

            for (int j = 0; j < this._dimension; j++) {
                int pos = (this._dimension + 2) * (i + 1) - (this._dimension + 1) + (j + 1);
                arg += pattern.get(j) * this._weights.get(pos - 1);
            }
            arg += this._weights.get((this._dimension + 2) * (i + 1) - 1);
            Double s = MathematicalFunctions.sig(arg);
            Double s1 = MathematicalFunctions.sigder(arg);
            patternDeriv.set((this._dimension + 2) * (i + 1) - (this._dimension + 1) - 1, s);
            patternDeriv.set((this._dimension + 2) * (i + 1) - 1,
                    this._weights.get((this._dimension + 2) * (i + 1) - (this._dimension + 1) - 1) * s1);
            for (int j = 0; j < this._dimension; j++) {
                int pos = (this._dimension + 2) * (i + 1) - (this._dimension + 1) + j;
                patternDeriv.set(pos - 1, this._weights.get((this._dimension + 2) * (i + 1) - (this._dimension + 1) - 1)
                        * pattern.get((j + 1) - 1) * s1);
            }
        }

        return patternDeriv;
    }

    private ArrayList<Double> getDeriv(ArrayList<Double> pattern, Double output) {
        ArrayList<Double> deriv = new ArrayList<>();
        this._weights.forEach(_item -> {
            deriv.add(0.0);
        });

        ArrayList<Double> patternDeriv = getPatternDeriv(pattern);
        Double wanted_output = pattern.get(this._dimension);

        for (int j = 0; j < deriv.size(); j++) {
            deriv.set(j, 2.0 * (output - wanted_output) * patternDeriv.get(j));
        }

        return deriv;
    }

    public Double train(Boolean geneticOption, GENETIC_CROSSOVER_OPTIONS geneticCrossoverOption) {
        this.initializeWeights(geneticOption, geneticCrossoverOption);
        this.findUniqueClasses();

        Double epochTrainError = -1.0;
        for (int i = 0; i < this._maxEpoches; i++) {
            for (int k = 0; k < this._patterns.size(); k++) {
                ArrayList<Double> pattern = this._patterns.get(k);
                Double output = getOutput(pattern);

                ArrayList<Double> deriv = getDeriv(pattern, output); // Back Propagation

                for (int j = 0; j < this._weights.size(); j++) { // Gradient Descent
                    this._weights.set(j, this._weights.get(j) - this._learningRate * deriv.get(j));
                }
            }

            epochTrainError = this.calculateMseWithoutLearning(this._patterns);

            if (this._wantToDisplayTrainErrorInEachEpoch) {
                this.displayTrainError(i, epochTrainError);
            }

            if (epochTrainError <= 0) {
                break;
            }
        }

        if (epochTrainError > -1) {
            System.out.println("******************");
        } else {
            System.out.println("Something went wrong!");
        }

        return epochTrainError;
    }

    public Double getTestError() {
        ArrayList<ArrayList<Double>> testPatterns = Data.getTestPatterns();

        if (testPatterns == null) {
            return -1.0;
        }

        return this.calculateMseWithoutLearning(testPatterns);
    }

    public Double calculateError(ArrayList<Double> weights) {
        if (weights == null) {
            return -1.0;
        }

        this._weights = new ArrayList<>(weights);

        Double sum = 0.0;

        for (int i = 0; i < this._patterns.size(); i++) {
            // The desired output
            Double wanted_output = this._patterns.get(i).get(this._dimension);
            Double output = getOutput(this._patterns.get(i));
            sum += (output - wanted_output) * (output - wanted_output);
        }

        return (sum / this._patterns.size());
    }

    private void displayTrainError(int i, Double trainError) {
        System.out.println("MLP Train Error: i[" + i + "] = " + trainError);
    }
}
