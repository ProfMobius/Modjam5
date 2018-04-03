package team.thegoldenhoe.cameraobscura.common.network;

public enum CameraTypes {
    VINTAGE(0, "camera_vintage"),
    POLAROID(1, "camera_polaroid"),
    DIGITAL(2, "camera_digital"),
    NOT_A_CAMERA(3, "nothing_what_would_we_even_need_this_for_lol");
	
	private int guiID;
	private String imagePath;
	public static CameraTypes[] VALUES = values();
	
	CameraTypes(int guiID, String imagePath) {
		this.guiID = guiID;
		this.imagePath = imagePath;
	}
	
	public static CameraTypes getCameraTypeByGuiID(int guiID) {
		for (CameraTypes type : VALUES) {
			if (type.guiID == guiID) {
				return type;
			}
		}
		return CameraTypes.NOT_A_CAMERA;
	}
	
	public String getImagePath() {
		return this.imagePath;
	}
	
	public int getGuiID() {
		return this.guiID;
	}
}
