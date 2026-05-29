package main;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;

public class SoundManager {

    private static Thread bossMusicThread = null;
    private static volatile boolean bossMusicPlaying = false;

    public static void playShoot()      { playTone(880, 60, 0.3f); }
    public static void playHit()        { playNoise(80, 0.4f); }
    public static void playEnemyDie()   { playTone(220, 120, 0.5f); }
    public static void playPlayerHit()  { playNoise(120, 0.6f); }

    public static void playBossSpawn() {
        new Thread(() -> {
            playTone(150, 200, 0.7f);
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            playTone(120, 300, 0.8f);
        }).start();
    }

    /** Son quand le boss entre en rage */
    public static void playBossRage() {
        new Thread(() -> {
            // Grondement grave ascendant
            for (int f = 60; f <= 200; f += 15) {
                playTone(f, 60, 0.8f);
                try { Thread.sleep(30); } catch (InterruptedException e) {}
            }
            playNoise(200, 0.9f);
        }).start();
    }

    public static void playWin() {
        new Thread(() -> {
            int[] notes = {523, 659, 784, 1047};
            for (int n : notes) {
                playTone(n, 150, 0.6f);
                try { Thread.sleep(120); } catch (InterruptedException e) {}
            }
        }).start();
    }

    /** Démarre la musique de boss en boucle (version standard) */
    public static void startBossMusic() {
        startMusicThread(false);
    }

    /** Démarre la musique finale du boss (plus intense, épique) */
    public static void startFinalBossMusic() {
        startMusicThread(true);
    }

    private static void startMusicThread(boolean finalMode) {
        if (bossMusicPlaying) return;
        bossMusicPlaying = true;
        bossMusicThread = new Thread(() -> {
            // Mélodie principale dramatique
            int[] melody;
            int[] duration;
            if (finalMode) {
                // Mélodie finale : plus rapide, plus intense, chromatique
                melody   = new int[]{330, 330, 494, 330, 392, 330, 370, 330,
                                     311, 330, 277, 294, 330, 247, 262, 294,
                                     220, 220, 330, 220, 277, 220, 247, 185};
                duration = new int[]{ 70,  70,  90,  70,  70,  70,  90,  70,
                                       70,  70,  90,  70,  90,  70,  70,  90,
                                       70,  70,  90,  70,  70,  70,  90,  140};
            } else {
                melody   = new int[]{220, 220, 330, 220, 277, 220, 247, 220, 208, 220, 185, 196};
                duration = new int[]{ 80,  80, 100,  80,  80,  80, 100,  80,  80,  80, 100,  80};
            }

            while (bossMusicPlaying && !Thread.currentThread().isInterrupted()) {
                for (int i = 0; i < melody.length && bossMusicPlaying; i++) {
                    playTone(melody[i], duration[i], finalMode ? 0.55f : 0.45f);
                    try { Thread.sleep(duration[i] + 8); } catch (InterruptedException ex) { break; }
                }
                // Pont dramatique
                if (bossMusicPlaying) {
                    if (finalMode) {
                        // Stinger épique de fin de boucle
                        playTone(110, 200, 0.7f);
                        try { Thread.sleep(100); } catch (InterruptedException ex) { break; }
                        playTone(165, 200, 0.65f);
                        try { Thread.sleep(150); } catch (InterruptedException ex) { break; }
                    } else {
                        playTone(110, 300, 0.55f);
                        try { Thread.sleep(200); } catch (InterruptedException ex) { break; }
                    }
                }
            }
        });
        bossMusicThread.setDaemon(true);
        bossMusicThread.start();
    }

    /** Arrête la musique de boss */
    public static void stopBossMusic() {
        bossMusicPlaying = false;
        if (bossMusicThread != null) {
            bossMusicThread.interrupt();
            bossMusicThread = null;
        }
    }

    private static void playTone(int freq, int durationMs, float volume) {
        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
                int samples = (int)(44100 * durationMs / 1000.0);
                byte[] buf = new byte[samples * 2];
                for (int i = 0; i < samples; i++) {
                    double angle = 2.0 * Math.PI * freq * i / 44100;
                    double env = Math.min(1.0, Math.min((double)i / (samples * 0.05),
                                                        (double)(samples - i) / (samples * 0.1)));
                    short val = (short)(Math.sin(angle) * 32767 * volume * env);
                    buf[i * 2]     = (byte)(val & 0xFF);
                    buf[i * 2 + 1] = (byte)((val >> 8) & 0xFF);
                }
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                if (!AudioSystem.isLineSupported(info)) return;
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();
                line.write(buf, 0, buf.length);
                line.drain();
                line.close();
            } catch (Exception ignored) {}
        }).start();
    }

    private static void playNoise(int durationMs, float volume) {
        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
                int samples = (int)(44100 * durationMs / 1000.0);
                byte[] buf = new byte[samples * 2];
                java.util.Random rnd = new java.util.Random();
                for (int i = 0; i < samples; i++) {
                    double env = Math.min(1.0, (double)(samples - i) / samples);
                    short val = (short)((rnd.nextDouble() * 2 - 1) * 32767 * volume * env);
                    buf[i * 2]     = (byte)(val & 0xFF);
                    buf[i * 2 + 1] = (byte)((val >> 8) & 0xFF);
                }
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                if (!AudioSystem.isLineSupported(info)) return;
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();
                line.write(buf, 0, buf.length);
                line.drain();
                line.close();
            } catch (Exception ignored) {}
        }).start();
    }
}
