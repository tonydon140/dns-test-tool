import json

import requests
from PySide6.QtCore import QRunnable, QObject, Signal
from ping3 import ping


class TestDelayRunnable(QRunnable):
    def __init__(self, index, ip, signal):
        super().__init__()
        self.index = index
        self.ip = ip
        self.signal = signal

    def run(self) -> None:
        total = 0
        success = 0
        fail = 0
        for i in range(4):
            delay = ping(self.ip)
            # 失败的情况
            if delay is None or delay is False or delay == 0:
                fail += 1
            else:
                success += 1
                total += delay
        mean_delay = 0 if success == 0 else total / success
        self.signal.emit(self.index, success, fail, mean_delay)


class GetDnsListRunnable(QRunnable):
    def __init__(self, signal):
        super().__init__()
        self.signal = signal

    def run(self) -> None:
        res = requests.get('https://tonydon.top:6515/admin/dns/list')
        res = json.loads(res.text)
        self.signal.emit(res['data'])


class CheckUpdateRunnable(QRunnable):
    def __init__(self, signal):
        super().__init__()
        self.signal = signal

    def run(self) -> None:
        res = requests.get("https://tonydon.top:6515/admin/project/version/latest/2")
        res = json.loads(res.text)
        self.signal.emit(res)


class MySignal(QObject):
    dns_item = Signal(int, int, int, float)
    dns_list = Signal(list)
    check_update = Signal(dict)
