package app.audio;

import javafx.concurrent.Task;

import javax.sound.sampled.*;

/**
 * Some audio capture code is from:
 * https://www.codejava.net/coding/capture-and-record-sound-into-wav-file-with-java-sound-api
 *
 * Algorithm used for converting raw audio to double is from:
 * https://stackoverflow.com/questions/3899585/microphone-level-in-java
 */
public class AudioCapture {
    private TargetDataLine _line;

    public Task callACTask() {
        setUpDataLine();
        return new AudioCaptureTask(_line);
    }

    /**
     * Defines an audio format
     */
    private AudioFormat getAudioFormat() {
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 2;
        int frameSize = 4;
        float frameRate = 44100;
        boolean bigEndian = false;
        AudioFormat format = new AudioFormat(encoding, sampleRate, sampleSizeInBits,
                channels, frameSize, frameRate, bigEndian);
        return format; //TODO: check these values with Catherine's slides
    }

    /**
     * Set up data line of the mic
     */
    private void setUpDataLine() {
        AudioFormat format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        // Checks if the system supports the data line
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Error: Line not supported");
            return;
        }

        try {
            _line = (TargetDataLine) AudioSystem.getLine(info);
            _line.open(format, 6000);
            _line.start();   // start capturing
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Class that handles capturing audio from the system's microphone.
     */
    private class AudioCaptureTask extends Task<Double> {
        private TargetDataLine targetLine;

        public AudioCaptureTask(TargetDataLine dataLine) {
            targetLine = dataLine;
        }

        @Override
        protected Double call() throws Exception {
            byte tempBuffer[] = new byte[6000];

            try {
                while (!isCancelled()) {
                    targetLine.read(tempBuffer, 0, tempBuffer.length);
                    double value = calculateRMSLevel(tempBuffer);
                    updateValue(value);
                }
                updateValue(0.0);
                targetLine.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0.0;
        }

        /**
         * Algorithm that converts the input data from the mic to a double value
         * @param audioData a byte array of the audio data
         * @return RMS level of raw audioData
         */
        public double calculateRMSLevel(byte[] audioData) {
            long lSum = 0;
            for(int i = 0; i < audioData.length; i++)
                lSum = lSum + audioData[i];

            double dAvg = lSum / audioData.length;
            double sumMeanSquare = 0d;

            for(int j = 0; j < audioData.length; j++)
                sumMeanSquare += Math.pow(audioData[j] - dAvg, 2d);

            double averageMeanSquare = sumMeanSquare / audioData.length;

            return (Math.pow(averageMeanSquare,0.5d) + 0.5);
        }
    }
}
