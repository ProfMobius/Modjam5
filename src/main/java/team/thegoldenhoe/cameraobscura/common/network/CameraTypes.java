package team.thegoldenhoe.cameraobscura.common.network;

public enum CameraTypes {
    VINTAGE(0),
    POLAROID(1),
    DIGITAL(2),
    NOT_A_CAMERA(3);
	
	private int guiID;
	
	CameraTypes(int guiID) {
		this.guiID = guiID;
	}
	
	public int getGuiID() {
		return this.guiID;
	}
}
