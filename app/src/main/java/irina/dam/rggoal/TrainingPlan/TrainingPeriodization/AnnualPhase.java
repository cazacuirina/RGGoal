package irina.dam.rggoal.TrainingPlan.TrainingPeriodization;

public class AnnualPhase {
    int phase;
    int intensity;
    int volume;
    int technicalPreparation;

    public AnnualPhase(){}

    public AnnualPhase(int phase, int intensity, int volume, int technicalPreparation) {
        this.phase = phase;
        this.intensity = intensity;
        this.volume = volume;
        this.technicalPreparation = technicalPreparation;
    }

    @Override
    public String toString() {
        return "AnnualPhase{" +
                "phase=" + phase +
                ", intensity=" + intensity +
                ", volume=" + volume +
                ", technicalPreparation=" + technicalPreparation +
                '}';
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getTechnicalPreparation() {
        return technicalPreparation;
    }

    public void setTechnicalPreparation(int technicalPreparation) {
        this.technicalPreparation = technicalPreparation;
    }
}
