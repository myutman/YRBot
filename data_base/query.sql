select offer_id, 
unified_address,
area,
total_price_in_rubbles,
rooms, 
partner_id, partner_name, agent_fee,  latitude, longitude, metro_geo_id, time_to_metro, floor,
floors_total,
price,
kitchen_area,
ceiling_height,
has_kitchen_furniture,
has_internet,
has_no_furniture,
has_phone,
has_refrigerator,
has_room_furniture,
has_television,
has_washing_machine,
built_year,
has_parking,
has_lift,
has_rubbish_chute,
is_guarded,
balcony,
bathroom_unit,
creation_date,
renovation,
floor_covering,
window_view,
studio,
open_plan
from 
	realty3_offers;
where
	cluster_head = True
	and error_code is null
	and is_premoderation = False
	and (category = 2 )
	and (type = 2 or type=1)
	and (price_period = 4 or price_period=100)
	and subject_federation_id = 10174
	
	
	
