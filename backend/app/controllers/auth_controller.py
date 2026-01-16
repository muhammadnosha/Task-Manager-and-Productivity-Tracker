from fastapi import HTTPException
from app.models.user import create_user, get_user_by_email

def signup_user(email: str, password: str, confirm_password: str):
    if password != confirm_password:
        raise HTTPException(status_code=400, detail="Passwords do not match")

    existing_user = get_user_by_email(email)
    if existing_user:
        raise HTTPException(status_code=400, detail="Email already registered")

   
    create_user(email, password)

    return {"message": "User registered successfully"}

def login_user(email: str, password: str):
    user = get_user_by_email(email)
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    if user['password'] != password:
        raise HTTPException(status_code=401, detail="Incorrect password")

    return {"message": "Login successful", "user_id": user['id'], "email": user['email']}