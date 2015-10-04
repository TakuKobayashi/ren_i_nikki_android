package com.nikki.i.ren.ren_i_nikki;

/**
 * カメラ情報
 */
public final class CameraInfo {
    /** ID */
    private String id;
    /** 名前 */
    private String name;
    /** MACアドレス */
    private String macAddress;
    /** 関連付けられているアプリケーションID */
    private String appID;
    /** 所有者の区分 */
    private int ownerType;
    /** 所有者のメールアドレス */
    private String ownerEmail;

    /**
     * コンストラクタ
     */
    public CameraInfo() {
    }

    /**
     * コンストラクタ
     */
    public CameraInfo(String id, String name, String macAddress, String appID, int ownerType, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.macAddress = macAddress;
        this.appID = appID;
        this.ownerType = ownerType;
        this.ownerEmail = ownerEmail;
    }

    /**
     * IDの設定
     * @param id ID
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * IDの取得
     * @return ID
     */
    public String getID() {
        return id;
    }

    /**
     * 名前の設定
     * @param name ID名前
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 名前の取得
     * @return 名前
     */
    public String getName() {
        return name;
    }

    /**
     * MACアドレスの設定
     * @param macAddress MACアドレス
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * MACアドレスの取得
     * @return MACアドレス
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * 関連付けられているアプリケーションIDの設定
     * @param appID 関連付けられているアプリケーションID
     */
    public void setAppID(String appID) {
        this.appID = appID;
    }

    /**
     * 関連付けられているアプリケーションIDの取得
     * @return 関連付けられているアプリケーションID
     */
    public String getAppID() {
        return appID;
    }

    /**
     * 所有者の区分の設定
     * @param ownerType 所有者の区分
     */
    public void setOwnerType(int ownerType) {
        this.ownerType = ownerType;
    }

    /**
     * 所有者の区分の取得
     * @return 所有者の区分
     */
    public int getOwnerType() {
        return ownerType;
    }

    /**
     * 所有者のメールアドレスの設定
     * @param ownerEmail 所有者のメールアドレス
     */
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    /**
     * 所有者のメールアドレスの取得
     * @return 所有者のメールアドレス
     */
    public String getOwnerEmail() {
        return ownerEmail;
    }
}
