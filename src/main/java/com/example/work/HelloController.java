package com.example.work;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class HelloController implements Initializable {

    private static final String MUSIC_DIRECTORY = "music";
    private static final int[] SPEEDS = {25, 50, 75, 100, 125, 150, 175, 200};
    private static final double VOLUME_SCALE = 0.01;

    @FXML
    private Pane musicPane;
    @FXML
    private Label currentSong;
    @FXML
    private Button playSongButton, stopSongButton, resetSongButton, previousSongButton, nextSongButton;
    @FXML
    private ComboBox<String> songSpeedComboBox;
    @FXML
    private Slider songVolumeSlider;
    @FXML
    private ProgressBar songProgressBar;

    private Media media;
    private MediaPlayer mediaPlayer;
    private ArrayList<File> songs;
    private int songNumber;
    private Timer timer;
    private boolean isTimerRunning;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSongs();
        initializeMediaPlayer();
        setupSongSpeedComboBox();
        setupVolumeControl();
        setupProgressBar();
    }

    private void loadSongs() {
        songs = new ArrayList<>();
        File directory = new File(MUSIC_DIRECTORY);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                songs.add(file);
            }
        }
    }

    private void initializeMediaPlayer() {
        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnEndOfMedia(this::playNextSong);
        currentSong.setText(songs.get(songNumber).getName());
    }

    private void setupSongSpeedComboBox() {
        for (int speed : SPEEDS) {
            songSpeedComboBox.getItems().add(speed + "%");
        }
        songSpeedComboBox.setOnAction(this::changeSpeed);
    }

    private void setupVolumeControl() {
        songVolumeSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            mediaPlayer.setVolume(newValue.doubleValue() * VOLUME_SCALE);
        });
    }

    private void setupProgressBar() {
        songProgressBar.setStyle("-fx-accent: #89CFF0;");
    }

    public void playSong() {
        startTimer();
        changeSpeed(null);
        mediaPlayer.play();
    }

    public void stopSong() {
        stopTimer();
        mediaPlayer.pause();
    }

    public void resetSong() {
        songProgressBar.setProgress(0);
        mediaPlayer.seek(Duration.seconds(0));
    }

    public void playNextSong() {
        Platform.runLater(() -> {
            songNumber = (songNumber < songs.size() - 1) ? songNumber + 1 : 0;
            reloadMediaPlayer();
            playSong();
        });
    }

    public void playPreviousSong() {
        Platform.runLater(() -> {
            songNumber = (songNumber > 0) ? songNumber - 1 : songs.size() - 1;
            reloadMediaPlayer();
            playSong();
        });
    }

    private void reloadMediaPlayer() {
        mediaPlayer.stop();
        stopTimer();
        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        currentSong.setText(songs.get(songNumber).getName());
    }

    public void changeSpeed(ActionEvent event) {
        if (songSpeedComboBox.getValue() == null) {
            mediaPlayer.setRate(1);
        } else {
            int speed = Integer.parseInt(songSpeedComboBox.getValue().replace("%", ""));
            mediaPlayer.setRate(speed * VOLUME_SCALE);
        }
    }

    private void startTimer() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                isTimerRunning = true;
                updateProgressBar();
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    private void updateProgressBar() {
        double current = mediaPlayer.getCurrentTime().toSeconds();
        double end = media.getDuration().toSeconds();
        songProgressBar.setProgress(current / end);

        if (current / end >= 1) {
            stopTimer();
            playNextSong();
        }
    }

    private void stopTimer() {
        isTimerRunning = false;
        if (timer != null) {
            timer.cancel();
        }
    }
}
