package com.android.gallery3d.common;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.drm.DrmStore.Action;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaMetadataRetriever;
import android.os.Build.VERSION;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import com.fyusion.sdk.viewer.internal.request.target.Target;

public class ApiHelper {
    public static final boolean API_VERSION_MIN_17;
    public static final boolean CAN_START_PREVIEW_IN_JPEG_CALLBACK;
    public static final int DRMSTORE_ACTION_SHOW_DIALOG = getIntFieldIfExists(Action.class, "SHOW_DIALOG", null, 256);
    public static final boolean ENABLE_PHOTO_EDITOR;
    public static final boolean HAS_ACTION_BAR;
    public static final boolean HAS_AUTO_FOCUS_MOVE_CALLBACK;
    public static final boolean HAS_CAMERA_FOCUS_AREA;
    public static final boolean HAS_CAMERA_HDR;
    public static final boolean HAS_CAMERA_METERING_AREA;
    public static final boolean HAS_EFFECTS_RECORDING_CONTEXT_INPUT;
    public static final boolean HAS_FACE_DETECTION;
    public static final boolean HAS_FINE_RESOLUTION_QUALITY_LEVELS;
    public static final boolean HAS_GET_CAMERA_DISABLED = hasMethod(DevicePolicyManager.class, "getCameraDisabled", ComponentName.class);
    public static final boolean HAS_GET_SUPPORTED_VIDEO_SIZE;
    public static final boolean HAS_IMAGES_COLUMNS_IS_HDR = hasField(ImageColumns.class, "IS_HDR");
    public static final boolean HAS_INTENT_EXTRA_LOCAL_ONLY;
    public static final boolean HAS_MEDIA_ACTION_SOUND;
    public static final boolean HAS_MEDIA_COLUMNS_HW_RECTIFY_OFFSET = hasField("com.huawei.android.provider.MediaStoreEx$MediaColumns", "HW_RECTIFY_OFFSET");
    public static final boolean HAS_MEDIA_COLUMNS_HW_REFOCUS_IMAGE = hasField("com.huawei.android.provider.MediaStoreEx$MediaColumns", "HW_IMAGE_REFOCUS");
    public static final boolean HAS_MEDIA_COLUMNS_HW_VOICE_OFFSET = hasField("com.huawei.android.provider.MediaStoreEx$MediaColumns", "HW_VOICE_OFFSET");
    public static final boolean HAS_MEDIA_COLUMNS_IS_HW_BURST = hasField("com.huawei.android.provider.MediaStoreEx$MediaColumns", "IS_HW_BURST");
    public static final boolean HAS_MEDIA_COLUMNS_IS_HW_FAVORITE = hasField("com.huawei.android.provider.MediaStoreEx$MediaColumns", "IS_HW_FAVORITE");
    public static final boolean HAS_MEDIA_COLUMNS_IS_PRIVACY = hasField("com.huawei.android.provider.MediaStoreEx$MediaColumns", "IS_HW_PRIVACY");
    public static final boolean HAS_MEDIA_COLUMNS_SPECIAL_FILE_OFFSET = hasField("com.huawei.android.provider.MediaStoreEx$MediaColumns", "SPECIAL_FILE_OFFSET");
    public static final boolean HAS_MEDIA_COLUMNS_SPECIAL_FILE_TYPE = hasField("com.huawei.android.provider.MediaStoreEx$MediaColumns", "SPECIAL_FILE_TYPE");
    public static final boolean HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT = hasField(MediaColumns.class, "WIDTH");
    public static final boolean HAS_MEDIA_MUXER = (VERSION.SDK_INT >= 18);
    public static final boolean HAS_MEDIA_PROVIDER_FILES_TABLE;
    public static final boolean HAS_MODIFY_STATUS_BAR_COLOR;
    public static final boolean HAS_MOTION_EVENT_TRANSFORM;
    public static final boolean HAS_MTP;
    public static final boolean HAS_MULTI_USER_STORAGE;
    public static final boolean HAS_OLD_PANORAMA;
    public static final boolean HAS_OPTIONS_IN_MUTABLE;
    public static final boolean HAS_POST_ON_ANIMATION;
    public static final boolean HAS_RELEASE_SURFACE_TEXTURE = hasMethod("android.graphics.SurfaceTexture", "release", new Class[0]);
    public static final boolean HAS_REMOTE_VIEWS_SERVICE;
    public static final boolean HAS_REUSING_BITMAP_IN_BITMAP_FACTORY;
    public static final boolean HAS_REUSING_BITMAP_IN_BITMAP_REGION_DECODER = (VERSION.SDK_INT >= 16);
    public static final boolean HAS_SET_BEAM_PUSH_URIS;
    public static final boolean HAS_SET_DEFALT_BUFFER_SIZE = hasMethod("android.graphics.SurfaceTexture", "setDefaultBufferSize", Integer.TYPE, Integer.TYPE);
    public static final boolean HAS_SET_ICON_ATTRIBUTE;
    public static final boolean HAS_SET_SYSTEM_UI_VISIBILITY = hasMethod(View.class, "setSystemUiVisibility", Integer.TYPE);
    public static final boolean HAS_SUPPORT_ROTATION_POLICY = hasMethod("com.android.internal.view.RotationPolicy", "isRotationSupported", Context.class);
    public static final boolean HAS_SURFACE_TEXTURE;
    public static final boolean HAS_SURFACE_TEXTURE_RECORDING;
    public static final boolean HAS_TIME_LAPSE_RECORDING;
    public static final boolean HAS_VIEW_FLAG_TRANSLUCENT_NAVIGATION;
    public static final boolean HAS_VIEW_FLAG_TRANSLUCENT_STATUS;
    public static final boolean HAS_VIEW_PROPERTY_ANIMATOR;
    public static final boolean HAS_VIEW_SYSTEM_UI_FLAG_HIDE_NAVIGATION = hasField(View.class, "SYSTEM_UI_FLAG_HIDE_NAVIGATION");
    public static final boolean HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE = hasField(View.class, "SYSTEM_UI_FLAG_LAYOUT_STABLE");
    public static final boolean HAS_VIEW_TRANSFORM_PROPERTIES;
    public static final boolean HAS_ZOOM_WHEN_RECORDING;
    public static final int META_DATA_RETRIEVER_ARGB8888_OPTION = getIntFieldIfExists(MediaMetadataRetriever.class, "OPTION_ARGB8888", null, Target.SIZE_ORIGINAL);
    public static final boolean SUPPORT_KEYGUARD_WALLPAPER_GG;
    public static final boolean USE_888_PIXEL_FORMAT;

    static {
        boolean z;
        boolean z2 = true;
        if (VERSION.SDK_INT >= 17) {
            z = true;
        } else {
            z = false;
        }
        API_VERSION_MIN_17 = z;
        if (VERSION.SDK_INT >= 17) {
            z = true;
        } else {
            z = false;
        }
        USE_888_PIXEL_FORMAT = z;
        if (VERSION.SDK_INT >= 14) {
            z = true;
        } else {
            z = false;
        }
        ENABLE_PHOTO_EDITOR = z;
        if (VERSION.SDK_INT >= 19) {
            z = true;
        } else {
            z = false;
        }
        HAS_VIEW_FLAG_TRANSLUCENT_NAVIGATION = z;
        if (VERSION.SDK_INT >= 19) {
            z = true;
        } else {
            z = false;
        }
        HAS_VIEW_FLAG_TRANSLUCENT_STATUS = z;
        if (VERSION.SDK_INT >= 21) {
            z = true;
        } else {
            z = false;
        }
        HAS_MODIFY_STATUS_BAR_COLOR = z;
        if (VERSION.SDK_INT >= 11) {
            z = true;
        } else {
            z = false;
        }
        HAS_REUSING_BITMAP_IN_BITMAP_FACTORY = z;
        if (VERSION.SDK_INT >= 16) {
            z = true;
        } else {
            z = false;
        }
        HAS_SET_BEAM_PUSH_URIS = z;
        if (VERSION.SDK_INT >= 11) {
            z = true;
        } else {
            z = false;
        }
        HAS_SURFACE_TEXTURE = z;
        if (VERSION.SDK_INT >= 12) {
            z = true;
        } else {
            z = false;
        }
        HAS_MTP = z;
        if (VERSION.SDK_INT >= 16) {
            z = true;
        } else {
            z = false;
        }
        HAS_AUTO_FOCUS_MOVE_CALLBACK = z;
        if (VERSION.SDK_INT >= 11) {
            z = true;
        } else {
            z = false;
        }
        HAS_REMOTE_VIEWS_SERVICE = z;
        if (VERSION.SDK_INT >= 11) {
            z = true;
        } else {
            z = false;
        }
        HAS_INTENT_EXTRA_LOCAL_ONLY = z;
        if (VERSION.SDK_INT >= 23) {
            z = true;
        } else {
            z = false;
        }
        HAS_MULTI_USER_STORAGE = z;
        if (VERSION.SDK_INT <= 23) {
            z = "N".equalsIgnoreCase(VERSION.RELEASE);
        } else {
            z = true;
        }
        SUPPORT_KEYGUARD_WALLPAPER_GG = z;
        boolean hasFaceDetection = false;
        try {
            Class<?> listenerClass = Class.forName("android.hardware.Camera$FaceDetectionListener");
            if (hasMethod(Camera.class, "setFaceDetectionListener", listenerClass) && hasMethod(Camera.class, "startFaceDetection", new Class[0]) && hasMethod(Camera.class, "stopFaceDetection", new Class[0])) {
                hasFaceDetection = hasMethod(Parameters.class, "getMaxNumDetectedFaces", new Class[0]);
                HAS_FACE_DETECTION = hasFaceDetection;
                if (VERSION.SDK_INT < 16) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_MEDIA_ACTION_SOUND = z;
                if (VERSION.SDK_INT < 14) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_OLD_PANORAMA = z;
                if (VERSION.SDK_INT < 11) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_TIME_LAPSE_RECORDING = z;
                if (VERSION.SDK_INT < 14) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_ZOOM_WHEN_RECORDING = z;
                if (VERSION.SDK_INT < 14) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_CAMERA_FOCUS_AREA = z;
                if (VERSION.SDK_INT < 14) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_CAMERA_METERING_AREA = z;
                if (VERSION.SDK_INT < 11) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_FINE_RESOLUTION_QUALITY_LEVELS = z;
                if (VERSION.SDK_INT < 11) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_MOTION_EVENT_TRANSFORM = z;
                if (VERSION.SDK_INT < 17) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_EFFECTS_RECORDING_CONTEXT_INPUT = z;
                if (VERSION.SDK_INT < 11) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_GET_SUPPORTED_VIDEO_SIZE = z;
                if (VERSION.SDK_INT < 11) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_SET_ICON_ATTRIBUTE = z;
                if (VERSION.SDK_INT < 11) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_MEDIA_PROVIDER_FILES_TABLE = z;
                if (VERSION.SDK_INT < 16) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_SURFACE_TEXTURE_RECORDING = z;
                if (VERSION.SDK_INT < 11) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_ACTION_BAR = z;
                if (VERSION.SDK_INT < 11) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_VIEW_TRANSFORM_PROPERTIES = z;
                if (VERSION.SDK_INT < 17) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_CAMERA_HDR = z;
                if (VERSION.SDK_INT < 11) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_OPTIONS_IN_MUTABLE = z;
                if (VERSION.SDK_INT < 14) {
                    z = false;
                } else {
                    z = true;
                }
                CAN_START_PREVIEW_IN_JPEG_CALLBACK = z;
                if (VERSION.SDK_INT < 12) {
                    z = false;
                } else {
                    z = true;
                }
                HAS_VIEW_PROPERTY_ANIMATOR = z;
                if (VERSION.SDK_INT < 16) {
                    z2 = false;
                }
                HAS_POST_ON_ANIMATION = z2;
            }
            hasFaceDetection = false;
            HAS_FACE_DETECTION = hasFaceDetection;
            if (VERSION.SDK_INT < 16) {
                z = true;
            } else {
                z = false;
            }
            HAS_MEDIA_ACTION_SOUND = z;
            if (VERSION.SDK_INT < 14) {
                z = true;
            } else {
                z = false;
            }
            HAS_OLD_PANORAMA = z;
            if (VERSION.SDK_INT < 11) {
                z = true;
            } else {
                z = false;
            }
            HAS_TIME_LAPSE_RECORDING = z;
            if (VERSION.SDK_INT < 14) {
                z = true;
            } else {
                z = false;
            }
            HAS_ZOOM_WHEN_RECORDING = z;
            if (VERSION.SDK_INT < 14) {
                z = true;
            } else {
                z = false;
            }
            HAS_CAMERA_FOCUS_AREA = z;
            if (VERSION.SDK_INT < 14) {
                z = true;
            } else {
                z = false;
            }
            HAS_CAMERA_METERING_AREA = z;
            if (VERSION.SDK_INT < 11) {
                z = true;
            } else {
                z = false;
            }
            HAS_FINE_RESOLUTION_QUALITY_LEVELS = z;
            if (VERSION.SDK_INT < 11) {
                z = true;
            } else {
                z = false;
            }
            HAS_MOTION_EVENT_TRANSFORM = z;
            if (VERSION.SDK_INT < 17) {
                z = true;
            } else {
                z = false;
            }
            HAS_EFFECTS_RECORDING_CONTEXT_INPUT = z;
            if (VERSION.SDK_INT < 11) {
                z = true;
            } else {
                z = false;
            }
            HAS_GET_SUPPORTED_VIDEO_SIZE = z;
            if (VERSION.SDK_INT < 11) {
                z = true;
            } else {
                z = false;
            }
            HAS_SET_ICON_ATTRIBUTE = z;
            if (VERSION.SDK_INT < 11) {
                z = true;
            } else {
                z = false;
            }
            HAS_MEDIA_PROVIDER_FILES_TABLE = z;
            if (VERSION.SDK_INT < 16) {
                z = true;
            } else {
                z = false;
            }
            HAS_SURFACE_TEXTURE_RECORDING = z;
            if (VERSION.SDK_INT < 11) {
                z = true;
            } else {
                z = false;
            }
            HAS_ACTION_BAR = z;
            if (VERSION.SDK_INT < 11) {
                z = true;
            } else {
                z = false;
            }
            HAS_VIEW_TRANSFORM_PROPERTIES = z;
            if (VERSION.SDK_INT < 17) {
                z = true;
            } else {
                z = false;
            }
            HAS_CAMERA_HDR = z;
            if (VERSION.SDK_INT < 11) {
                z = true;
            } else {
                z = false;
            }
            HAS_OPTIONS_IN_MUTABLE = z;
            if (VERSION.SDK_INT < 14) {
                z = true;
            } else {
                z = false;
            }
            CAN_START_PREVIEW_IN_JPEG_CALLBACK = z;
            if (VERSION.SDK_INT < 12) {
                z = true;
            } else {
                z = false;
            }
            HAS_VIEW_PROPERTY_ANIMATOR = z;
            if (VERSION.SDK_INT < 16) {
                z2 = false;
            }
            HAS_POST_ON_ANIMATION = z2;
        } catch (Throwable th) {
        }
    }

    public static int getIntFieldIfExists(Class<?> klass, String fieldName, Class<?> obj, int defaultVal) {
        try {
            return klass.getDeclaredField(fieldName).getInt(obj);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static boolean hasField(Class<?> klass, String fieldName) {
        try {
            klass.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private static boolean hasField(String className, String fieldName) {
        try {
            Class.forName(className).getDeclaredField(fieldName);
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    private static boolean hasMethod(String className, String methodName, Class<?>... parameterTypes) {
        try {
            Class.forName(className).getDeclaredMethod(methodName, parameterTypes);
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    private static boolean hasMethod(Class<?> klass, String methodName, Class<?>... paramTypes) {
        try {
            klass.getDeclaredMethod(methodName, paramTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
