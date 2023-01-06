package gilp.measure;

public class Values {
     double tp;
    double fp;
    double tn;
    double fn;

    public Values() {

    }
    public Values(double tp, double fp, double tn, double fn) {

        this.tp = tp;
        this.fp = fp;
        this.tn = tn;
        this.fn = fn;

        }

    public  double getPrecision() {

            return tp/(tp+fp);
        }

    public  double getTn() {

        return tn;
    }
    public  double getTP() {

        return tp;
    }
    public  double getFp() {

        return fp;
    }
    public  double getFn() {

        return fn;
    }


    public  double getRecall() {
            return (tp + tn) / (tp + tn + fp + fn);
        }
    public double getAccuracy() {

        return tp/(tp+fn);
    }

    public double getF1() {
        double precision = getPrecision();
        double recall = getRecall();

            return (2 * precision * recall) / (precision + recall);
        }



}
