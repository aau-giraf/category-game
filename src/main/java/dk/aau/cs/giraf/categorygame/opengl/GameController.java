package dk.aau.cs.giraf.categorygame.opengl;

import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.categorygame.GameConfiguration;
import dk.aau.cs.giraf.categorygame.StationConfiguration;
import dk.aau.cs.giraf.categorygame.opengl.game.GameData;

public class GameController {

    private GameActivity gameActivity;
    private GameConfiguration gameConfiguration;
    private GameData gameData;
    private int numberOfPictoFrames;

    public boolean IsTrainStopped;

    public GameController(GameActivity gameActivity, GameConfiguration gameConfiguration, GameData gameData) {
        this.gameActivity = gameActivity;
        this.gameConfiguration = gameConfiguration;
        this.gameData = gameData;
    }

    // Checks if this station is for onloading
    private boolean IsLoadingStation(StationConfiguration station, ArrayList<StationLinearLayout> stationLinear)
    {
        return station.isLoadingStation();
    }
    private boolean checkPictogramsIsOnStation(StationConfiguration station, ArrayList<StationLinearLayout> stationLinear){
        boolean answer = false;
        int acceptedPics = 0;
        int totalPictogramsOnStaion = 0;
        for (StationLinearLayout stationLin : stationLinear) {
            totalPictogramsOnStaion += stationLin.getPictograms().size();
        }

        for (long pictoId : station.getAcceptPictograms()){
            boolean foundPic = false;
            for(StationLinearLayout stationLin : stationLinear){
                for (PictoFrameLayout pictoFrame : stationLin.getPictoframes()) {
                    if(pictoFrame.getChildCount() > 0){
                        if(pictoId == pictoFrame.getPictogram().getPictogramID() && pictoFrame.getPictogram().getTag() != "found"){
                            acceptedPics++;
                            pictoFrame.getPictogram().setTag("found");
                            foundPic = true;
                            break;
                        }
                    }

                }
                if(foundPic == true){
                    break;
                }
            }
        }

        if(acceptedPics == station.getAcceptPictograms().size() && station.getAcceptPictograms().size() == totalPictogramsOnStaion ){
            answer = true;
        }
        else{
            for (StationLinearLayout stationLin : stationLinear) {
                for (PictoFrameLayout pictoFrame : stationLin.getPictoframes()) {
                    if(pictoFrame.getChildCount() > 0){
                        pictoFrame.getPictogram().setTag(null);
                    }
                }
            }
        }

        return answer;
    }

    public void trainDrive(ArrayList<StationLinearLayout> stationLinear){
        if(this.gameData.currentTrainVelocity == 0f && this.gameData.numberOfStops < this.gameData.numberOfStations) {//pga. remise
            boolean readyToGo = true;


            //if(IsLoadingStation(this.gameConfiguration.getStation(this.gameData.numberOfStops - 1), stationLinear))
            //if(this.gameData.numberOfStops == 0)
            if(
                    this.gameData.numberOfStops == 0 ||
                            IsLoadingStation(this.gameConfiguration.getStation(this.gameData.numberOfStops - 1), stationLinear)
                    )
            {
                // if this is the first station
                for (LinearLayout lin : stationLinear) {
                    for (int i = 0; i< lin.getChildCount();i++) {
                        FrameLayout frame = (FrameLayout)lin.getChildAt(i);
                        if(frame.getChildAt(0) != null){
                            readyToGo = false;
                        }
                    }
                }
            }
            else {
                //check if it is the correct pictogram on the right station.
                if(checkPictogramsIsOnStation(this.gameConfiguration.getStation(this.gameData.numberOfStops - 1), stationLinear) ==  false){
                    readyToGo = false;
                }
            }

            if(readyToGo){

                //numberOfPictoFrames = (gameConfiguration.getNumberOfPictogramsOfStations() <= 4) ? 4:6;
                //numberOfPictoFrames = gameConfiguration.getNumberOfPictogramsOfStations();
                numberOfPictoFrames = (gameConfiguration.getNumberOfPictogramsOfStations());
                if (numberOfPictoFrames < 4) numberOfPictoFrames = 4;
                else if (numberOfPictoFrames % 2 == 1) numberOfPictoFrames += 1;
                Pictogram[] PictogramsOnStation = new Pictogram[numberOfPictoFrames];
                int index = 0;

                for (StationLinearLayout stationLin : stationLinear) {
                    for (PictoFrameLayout pictoFrame : stationLin.getPictoframes()) {
                        PictogramsOnStation[index] = (Pictogram)pictoFrame.getChildAt(0);
                        index++;
                    }
                }

                this.gameData.setStationPictograms(this.gameData.numberOfStops, PictogramsOnStation);

                if(this.gameData.numberOfStops + 1 == this.gameData.numberOfStations ){ //last station
                    this.gameActivity.hideAllLinearLayouts();
                }else{
                    this.gameActivity.hideStationLinearLayouts();
                }

                this.gameActivity.deletePictogramsFromStation();

                this.gameActivity.getGameData().accelerateTrain();

                this.gameActivity.hideSystemUI();

                this.gameActivity.streamId = this.gameActivity.soundPool.play(this.gameActivity.sound, 1f, 1f, 0, 0, 0.5f);
            }
        }
    }


    public void TrainIsStopping(){
        if(this.gameData.numberOfStops != this.gameData.numberOfStations ){ //do not show when train is at remise
            this.gameActivity.showStationLinearLayouts();
        }else{
            this.gameActivity.addAndShowEndButton(); //show end game button
        }

        IsTrainStopped = true;
    }


}