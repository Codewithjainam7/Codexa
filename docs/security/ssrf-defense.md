# SSRF Defense in Remote Repository Fetching

All outbound GitHub connections validate target IP addresses against private subnets (RFC 1918, RFC 3927, loopback).
