package com.example.musictimer

const val TIMER_NOT_STARTED = 303
const val TIMER_RUNNING = 103
const val TIMER_STOPPED = 203

const val SELECTED_THEME_ID = "selected_theme_position_on_editing_theme"
const val SELECTED_THEME_ID_NOT_SET: Long = -123

const val SELECTED_THEME_INFORMATION_ENTITY_ID: Long = 1

const val ADD_TRACK_TO_THEME_REQUEST_CODE = 223
const val ADD_TRACK_TO_THEME_NEW_TRACKS_ID_ARRAY = "array_with_id_to_editing_themes"
const val ADD_TRACK_TO_THEME_SAVING_INTENT_TRACKS_IDS =
    "ids_of_tracks_to_save_add_tracks_to_theme_selected_tracks"

const val ACTUAL_PLAYING_TRACK_NAME_BLANK = "actual_playing_track_is_null_so_not_playing_any_track"

const val LOAD_TRACKS_SERVICE_STARTED_FROM_ALARM =
    "intent_extra_defines_if_service_is_started_from_alarm"
const val MAIN_FOREGROUND_SERVICE_CHANNEL_ID = "timer_and_music_player_foreground_service_channel"

const val MEDIA_BUTTON_ACTION = "intent_extra_specified_media_button_action"
const val MUSIC_PLAYER_PREVIOUS_TRACK = 501
const val MUSIC_PLAYER_STOP_TRACK = 502
const val MUSIC_PLAYER_START_TRACK = 503
const val MUSIC_PLAYER_NEXT_TRACK = 504