-- Clean up any old data
delete from TripNotificationList
delete from ShareableTrip
delete from TripBase -- not deleted automatically when shareable trip deleted?
delete from AuthenticatedUser

-- add test data
insert into [AuthenticatedUser] (firstName, lastName, id, authId, authToken, lastAuthCheck) values ('first', 'user', 'dravnaas', 'dravnaas', 'DoyleRavnaasAuth', CURRENT_TIMESTAMP);

DECLARE @lastMonth as DATETIME = DATETIMEFROMPARTS ( 2014, 11, 26, 12, 00, 00, 00 )
insert into [AuthenticatedUser] (id, authId, authToken, lastAuthCheck) values ('malcolm', 'malcolm', 'MalcolmAuth', @lastMonth);

DECLARE @fbuserAuthId as varchar(255) = '10152874890039731'
DECLARE @fbFakeToken as varchar(255) = 'whatever'
insert into [AuthenticatedUser] (id, authId, authToken, lastAuthCheck) values ('doylerav', @fbuserAuthId, @fbFakeToken, CURRENT_TIMESTAMP);

insert into [TripBase] (TRIP_TYPE, destination, startDate, friendlyName) values ('ShareableTrip', 'tacoma', getdate(), 'my trip to tacoma');
DECLARE @trip1Id AS int = @@Identity
insert into [ShareableTrip] (id, tripOwner) values (@trip1Id, 'doylerav');

insert into [TripBase] (TRIP_TYPE, destination, startDate, friendlyName) values ('ShareableTrip', 'wintrhop', getdate(), 'my trip to wintrhop');
DECLARE @trip2Id AS int = @@Identity
insert into [ShareableTrip] (id, tripOwner) values (@trip2Id, 'dravnaas');

insert into [TripNotificationList] (containingTrip) values (1)
DECLARE @noteList1 AS int = @@Identity

insert into [TripNotificationList] (containingTrip) values (@trip2Id)
DECLARE @noteList2 AS int = @@Identity

select * from TripNotificationList
select * from ShareableTrip
select * from TripBase
select * from AuthenticatedUser
select * from SharedTrip


select * from EnhancedLinkDetail


--delete from SharedTrip where userId=4
delete from Authenticateduser where id=4 -- this deletes shared trips as expected

select * from FlightServiceDetail
select * from FlightDetail
select * from TripDetail

/*
delete from FlightDetail
delete from TripDetail where DETAIL_TYPE='FlightDetail'
delete from FlightServiceDetail
*/

delete from FlightDetail where id=4
delete from TripDetail where id=4
delete from FlightServiceDetail where id = 24


select * from EnhancedLinkDetail

select * from TripNotification
select * from TripNotificationList
select * from SharedTrip
select * from ShareableTrip
select * from TripBase

DECLARE @deleteTripId as int =4
delete from TripNotification where listId=4
delete from TripNotificationList where containingTrip = @deleteTripId
delete from ShareableTrip where id=@deleteTripId
delete from TripBase where id=@deleteTripId

select * from TripNotificationList
select * from TripNotification

select * from FlightServiceDetail
update FlightServiceDetail
set arrivalTime = getdate()
from FlightServiceDetail
where arrivalCity = 'SFO'

select * from FlightDetail