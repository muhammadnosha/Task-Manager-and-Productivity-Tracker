from fastapi import APIRouter, HTTPException, Query
from app.controllers import export_controller

router = APIRouter()

@router.get("/{user_id}")
def export_user_tasks(user_id: int, format: str = Query(..., min_length=3, max_length=3)):
    """
    Endpoint to export a user's tasks as a file.
    Requires a 'format' query parameter ('csv' or 'pdf').
    """
    if format.lower() == 'csv':
        return export_controller.generate_csv_of_tasks(user_id)
    elif format.lower() == 'pdf':
        return export_controller.generate_pdf_of_tasks(user_id)
    else:
        raise HTTPException(status_code=400, detail="Invalid format specified. Use 'csv' or 'pdf'.")