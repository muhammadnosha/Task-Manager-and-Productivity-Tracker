from fastapi import APIRouter
from pydantic import BaseModel, EmailStr
from app.controllers.auth_controller import signup_user, login_user 

router = APIRouter()

class SignUpRequest(BaseModel):
    email: EmailStr
    password: str
    confirmPassword: str

class LoginRequest(BaseModel):
    email: EmailStr
    password: str

@router.post("/signup")
def signup(request: SignUpRequest):
    return signup_user(request.email, request.password, request.confirmPassword)

@router.post("/login")
def login(request: LoginRequest):
    return login_user(request.email, request.password)