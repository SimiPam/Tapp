package com.example.pam.tapp;

public class FinalEmotions {
    private String result="neutral", textEmotion="neutral";
    private String  smileProb="neutral";

    public FinalEmotions() {

    }

    public FinalEmotions(String result, String textEmotion, String smileProb) {
        this.result = getEMotion(textEmotion,smileProb);
        this.textEmotion = textEmotion;
        this.smileProb = smileProb;
    }

    //4/28/2019

    public String getEMotion(String textEmotion, String smileProb)
    {
        this.textEmotion = textEmotion;
        this.smileProb = smileProb;
        result=getEMotion();
        this.result=result;
        return result;

    }

    public String getEMotion()
    {

        switch(textEmotion)
        {
            case "joy":
                if (smileProb.equals("CALM"))
                { result = "Joy"; }
                else if(smileProb.equals("SURPRISED"))
                { result = "Happy"; }
                else if (smileProb.equals("SAD"))
                { result = "neutral"; }
                else if (smileProb.equals("HAPPY"))
                { result = "Joy"; }
                else if (smileProb.equals("ANGRY"))
                { result = "Sarcastic"; }
                else if (smileProb.equals("CONFUSED"))
                { result = "Joy/Confused"; }
                else if (smileProb.equals("DISGUSTED"))
                { result = "happiness"; }
                break;
            case "fear":
                if (smileProb.equals("CALM"))
                { result = "fear"; }
                else if(smileProb.equals("SURPRISED"))
                { result = "fear"; }
                else if (smileProb.equals("SAD"))
                { result = "fear/sad"; }
                else if (smileProb.equals("HAPPY"))
                { result = "Joking-fear/fear"; }
                else if (smileProb.equals("ANGRY"))
                { result = "fear"; }
                else if (smileProb.equals("CONFUSED"))
                { result = "fear"; }
                else if (smileProb.equals("DISGUSTED"))
                { result = "angry"; }
                break;
            case "sadness":
                if (smileProb.equals("CALM"))
                { result = "sad"; }
                else if(smileProb.equals("SURPRISED"))
                { result = "sad"; }
                else if (smileProb.equals("SAD"))
                { result = "sad"; }
                else if (smileProb.equals("HAPPY"))
                { result = "neutral"; }
                else if (smileProb.equals("ANGRY"))
                { result = "angry"; }
                else if (smileProb.equals("CONFUSED"))
                { result = "sad"; }
                else if (smileProb.equals("DISGUSTED"))
                { result = "angry"; }
                break;
            case "anger":
                if (smileProb.equals("CALM"))
                { result = "angry"; }
                else if(smileProb.equals("SURPRISED"))
                { result = "angry"; }
                else if (smileProb.equals("SAD"))
                { result = "angry"; }
                else if (smileProb.equals("HAPPY"))
                { result = "neutral"; }
                else if (smileProb.equals("ANGRY"))
                { result = "angry"; }
                else if (smileProb.equals("CONFUSED"))
                { result = "disgusted"; }
                else if (smileProb.equals("DISGUSTED"))
                { result = "disgusted"; }
                break;
            case "analytical":  //A person's reasoning and analytical attitude about things
                if (smileProb.equals("CALM"))
                { result = "neutral"; }
                else if(smileProb.equals("SURPRISED"))
                { result = "analytical"; }
                else if (smileProb.equals("SAD"))
                { result = "analytical"; }
                else if (smileProb.equals("HAPPY"))
                { result = "neutral"; }
                else if (smileProb.equals("ANGRY"))
                { result = "analytical"; }
                else if (smileProb.equals("CONFUSED"))
                { result = "analytical/Confused"; }
                else if (smileProb.equals("DISGUSTED"))
                { result = "analytical"; }
                break;
            case "confident":   //A person's degree of certainty
                if (smileProb.equalsIgnoreCase("CALM"))
                { result = "confident"; }
                else if(smileProb.equalsIgnoreCase("SURPRISED"))
                { result = "confident"; }
                else if (smileProb.equalsIgnoreCase("SAD"))
                { result = "neutral/tentative"; }
                else if (smileProb.equalsIgnoreCase("HAPPY"))
                { result = "confident"; }
                else if (smileProb.equalsIgnoreCase("ANGRY"))
                { result = "confident"; }
                else if (smileProb.equalsIgnoreCase("CONFUSED"))
                { result = "tentative"; }
                else if (smileProb.equalsIgnoreCase("DISGUSTED"))
                { result = "confident"; }
                break;
            case "tentative":
                if (smileProb.equalsIgnoreCase("CALM"))
                { result = "confident"; }
                else if(smileProb.equalsIgnoreCase("SURPRISED"))
                { result = "tentative"; }
                else if (smileProb.equalsIgnoreCase("SAD"))
                { result = "tentative"; }
                else if (smileProb.equalsIgnoreCase("HAPPY"))
                { result = "tentative"; }
                else if (smileProb.equalsIgnoreCase("ANGRY"))
                { result = "Sarcastic"; }
                else if (smileProb.equalsIgnoreCase("CONFUSED"))
                { result = "confused"; }
                else if (smileProb.equalsIgnoreCase("DISGUSTED"))
                { result = "disgusted"; }
                break;

            default:
                System.out.println("no match");
        }


        return result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTextEmotion() {
        return textEmotion;
    }

    public void setTextEmotion(String textEmotion) {
        this.textEmotion = textEmotion;
    }

    public String getSmileProb() {
        return smileProb;
    }

    public void setSmileProb(String smileProb) {
        this.smileProb = smileProb;
    }
}
