package com.example.pam.tapp;

public class FinalEmotions {
    private String result, textEmotion;
    private float smileProb;

    public FinalEmotions() {

    }

    public FinalEmotions(String result, String textEmotion, float smileProb) {
        this.result = getEMotion(textEmotion,smileProb);
        this.textEmotion = textEmotion;
        this.smileProb = smileProb;
    }

    public String getEMotion(String textEmotion, float smileProb)
    {
        switch(textEmotion)
        {
            case "happiness":
                if (smileProb <0.35)
                {
                    result = "neutral";
                }
                else if (smileProb >0.66)
                {
                    result = "happiness";
                }
                else
                {
                    result = "happiness";
                }
                break;
            case "angry":
                if (smileProb <0.35)
                {
                    result = "neutral";
                }
                else if (smileProb >0.66)
                {
                    result = "angry";
                }
                else
                {
                    result = "angry";
                }
                break;
            case "surprise":
                if (smileProb <0.35)
                {
                    result = "happiness";
                }
                else if (smileProb >0.66)
                {
                    result = "surprise";
                }
                else
                {
                    result = "surprise";
                }
                break;
            case "neutral":
                if (smileProb <0.35)
                {
                    result = "neutral";
                }
                else if (smileProb >0.66)
                {
                    result = "happiness";
                }
                else
                {
                    result = "happiness";
                }
                break;
            case "fear":
                if (smileProb <0.35)
                {
                    result = "fear";
                }
                else if (smileProb >0.66)
                {
                    result = "neutral";
                }
                else
                {
                    result = "neutral";
                }
                break;
            case "disgust":
                if (smileProb <0.35)
                {
                    result = "disgust";
                }
                else if (smileProb >0.66)
                {
                    result = "angry";
                }
                else
                {
                    result = "disgust";
                }
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

    public float getSmileProb() {
        return smileProb;
    }

    public void setSmileProb(float smileProb) {
        this.smileProb = smileProb;
    }
}
