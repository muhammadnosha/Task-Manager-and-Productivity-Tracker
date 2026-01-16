from fastapi import APIRouter
from app.controllers import task_controller

router = APIRouter()

@router.get("/productivity/{user_id}")
def get_productivity_data(user_id: int):
    """
    Endpoint to get productivity data for a user.
    Returns a list of objects, each containing a date and the total seconds worked on that day.
    """
    return task_controller.get_productivity_stats(user_id)

@router.get("/summary/daily/{user_id}")
def get_daily_summary_data(user_id: int):
    """
    Endpoint to get a summary of total productive time for today.
    Returns an object with the total seconds worked on tasks completed today.
    """
    return task_controller.get_daily_summary(user_id)