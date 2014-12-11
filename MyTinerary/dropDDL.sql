ALTER TABLE TripBase DROP CONSTRAINT UNQ_TripBase_0
ALTER TABLE ShareableTrip DROP CONSTRAINT FK_ShareableTrip_id
ALTER TABLE ShareableTrip DROP CONSTRAINT ShareableTriptripOwner
ALTER TABLE SharedTrip DROP CONSTRAINT SharedTrip_sharedUser
ALTER TABLE SharedTrip DROP CONSTRAINT FK_SharedTrip_trip
ALTER TABLE SharedTrip DROP CONSTRAINT UNQ_SharedTrip_0
ALTER TABLE SubTrip DROP CONSTRAINT FK_SubTrip_id
ALTER TABLE SubTrip DROP CONSTRAINT SubTripSUBTRIPDETAILid
ALTER TABLE TripDetail DROP CONSTRAINT FK_TripDetail_tripId
ALTER TABLE TripDetail DROP CONSTRAINT UNQ_TripDetail_0
ALTER TABLE ToDoListDetail DROP CONSTRAINT FK_ToDoListDetail_id
ALTER TABLE ToDoListItem DROP CONSTRAINT FK_ToDoListItem_listId
ALTER TABLE ReservationDetail DROP CONSTRAINT ReservationDetail_id
ALTER TABLE SubtripDetail DROP CONSTRAINT FK_SubtripDetail_id
ALTER TABLE FlightDetail DROP CONSTRAINT FlghtDtlflghtSvcDtilId
ALTER TABLE FlightDetail DROP CONSTRAINT FK_FlightDetail_id
ALTER TABLE ActivityLinkDetail DROP CONSTRAINT ctvtyLnkDtailnhncdDtld
ALTER TABLE ActivityLinkDetail DROP CONSTRAINT ActivityLinkDetail_id
ALTER TABLE FlightServiceDetail DROP CONSTRAINT FlightServiceDetail0
ALTER TABLE TripNotification DROP CONSTRAINT TripNotificationlistId
ALTER TABLE TripNotificationList DROP CONSTRAINT TrpNtfctnListcntnngTrp
ALTER TABLE TripNotificationList DROP CONSTRAINT TripNotificationList0
DROP TABLE AuthenticatedUser
DROP TABLE TripBase
DROP TABLE ShareableTrip
DROP TABLE SharedTrip
DROP TABLE SubTrip
DROP TABLE TripDetail
DROP TABLE ToDoListDetail
DROP TABLE ToDoListItem
DROP TABLE ReservationDetail
DROP TABLE SubtripDetail
DROP TABLE FlightDetail
DROP TABLE ActivityLinkDetail
DROP TABLE EnhancedLinkDetail
DROP TABLE FlightServiceDetail
DROP TABLE TripNotification
DROP TABLE TripNotificationList
